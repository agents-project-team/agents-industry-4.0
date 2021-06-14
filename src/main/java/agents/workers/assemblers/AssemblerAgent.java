package agents.workers.assemblers;

import agents.configs.SimulationConfig;
import agents.events.Event;
import agents.events.EventType;
import agents.product.PartPlan;
import agents.product.Product;
import agents.product.ProductPart;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.Worker;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblerAgent extends Worker<AssemblerState> {

	private AssemblerType assemblerType;

	private boolean isAlive;

	private List<ProductPlan> currentPlans = new ArrayList<>();

	private List<ProductPart> storedParts = new ArrayList<>();

	@Override
	public void setup() {
		super.setup();
		assemblerType = AssemblerType.getByName(getWorkerType());
		isAlive = true;
		setupWorkerCommunicationBehaviour();
	}

	private void setupWorkerCommunicationBehaviour() {
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if(isAlive){
						if (msg.getPerformative() == ACLMessage.INFORM) {
							if(msg.getProtocol().equals("PPLAN")) {
								Logger.info(getLocalName() + " has received the plan!");

								currentPlans.add(JsonConverter.fromJsonString(msg.getContent(), ProductPlan.class));
								currentPlans.sort(Comparator.comparing(ProductPlan::getPriority, Comparator.reverseOrder()));
							} else if(msg.getProtocol().equals("REG")){
								registerAgent("Assembler");
							} else if (msg.getProtocol().equals("ACT")){
								Logger.process(getLocalName() + " replaces broken machine.");
								Event.createEvent(new Event(EventType.AGENT_REPLACED, getAID(), getAgentCurrentContainerName(), ""));
								moveToMainContainer();
								registerAgent("Assembler");
							}
						} else if (msg.getPerformative() == ACLMessage.UNKNOWN) {
							if (msg.getProtocol().equals("SPART")) {
								ProductPart receivedPart = JsonConverter.fromJsonString(msg.getContent(), ProductPart.class);
								Logger.info(getLocalName() + " has received new part from " + msg.getSender().getLocalName());
								Event.createEvent(new Event(EventType.PART_RECEIVED, getAID(), getAgentCurrentContainerName(), ""));

								storedParts.add(receivedPart);
								assembleParts();
							} else if (msg.getProtocol().equals("PPROD")){
								Logger.info(getLocalName() + " has received new partial product from " + msg.getSender().getLocalName());
								Event.createEvent(new Event(EventType.PART_RECEIVED, getAID(), getAgentCurrentContainerName(), ""));

								Product partialProduct = JsonConverter.fromJsonString(msg.getContent(), Product.class);
								storedParts.addAll(partialProduct.getProductParts());
								assembleParts();
							}
						} else if (msg.getPerformative() == ACLMessage.REQUEST) {
							AssemblerState assemblerState = JsonConverter.fromJsonString(msg.getContent(), AssemblerState.class);
							currentPlans = assemblerState.getCurrentPlans();
							storedParts = assemblerState.getStoredParts();
							Logger.info(getLocalName() + " has received unfinished parts from broken assembler");

							assembleParts();
						}
					}else{
						//Reroute message to current assembler
						System.out.println("*********************** Message Rerouted ***********************");
						ACLMessage rerouteMsg = new ACLMessage(msg.getPerformative());
						rerouteMsg.setProtocol(msg.getProtocol());
						rerouteMsg.setContent(msg.getContent());
						AID receiverAID = getAssemblerAID(assemblerType);
						rerouteMsg.addReceiver(receiverAID);
						send(rerouteMsg);
					}
				} else {
					block();
				}
			}
		});
	}

	@Override
	public AssemblerState getUnfinishedTask() {
		return new AssemblerState(currentPlans, storedParts);
	}

	private void assembleParts() {
		List<ProductPlan> currentPlansCopy = new ArrayList<>(this.currentPlans);
		for (ProductPlan plan : currentPlansCopy) {
			List<ProductPart> parts = getStorageParts(plan);
			if (parts.size() > 0) {
				Logger.info(getLocalName() + " is assembling part");

				if(!assembleProcedure()){
					isAlive = false;
					return;
				}

				Event.createEvent(new Event(EventType.PRODUCT_ASSEMBLED, getAID(), getAgentCurrentContainerName(), ""));
				storedParts.removeAll(parts);
				sendParts(parts);
				plan.decreaseAllAmounts();
				if (plan.getCurrentAmount() == 0) {
					this.currentPlans.remove(plan);
				}
			}
		}
	}

	private List<ProductPart> getStorageParts(ProductPlan plan) {
		List<ProductPart> partsToSend = new ArrayList<>();
		Set<PartPlan> planProductParts = new HashSet<>(plan.getPlanParts().values());

		for (PartPlan partPlan : planProductParts) {
			for (ProductPart part : storedParts) {
				if (partPlan.getPartType().equals(part.getType())) {
					partsToSend.add(part);
					break;
				}
			}
		}
		if (!(partsToSend.size() == plan.getPlanParts().values().size())) {
			partsToSend.clear();
		}

		return partsToSend;
	}

	private void sendParts(List<ProductPart> parts) {
		if (assemblerType == AssemblerType.Final) {
			Product product = new Product(1, parts);
			ACLMessage msgToAssemblerManager = new ACLMessage(ACLMessage.UNKNOWN);
			msgToAssemblerManager.setProtocol("FPROD");
			msgToAssemblerManager.addReceiver(getManagerId());
			msgToAssemblerManager.setContent(JsonConverter.toJsonString(product));
			send(msgToAssemblerManager);
		} else {
			AID receiverAID = getAssemblerAID(AssemblerType.Final);
			if(receiverAID!=null){
				Product partialProduct = new Product(1, parts);
				ACLMessage partsToFinalAssembler = new ACLMessage(ACLMessage.UNKNOWN);
				partsToFinalAssembler.setProtocol("PPROD");
				partsToFinalAssembler.setContent(JsonConverter.toJsonString(partialProduct));
				partsToFinalAssembler.addReceiver(receiverAID);
				send(partsToFinalAssembler);
			}
		}
	}

	private AID getAssemblerAID(AssemblerType destinationType){
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Assembler"+destinationType.toString());
		sd.setType("Assembler"+destinationType.toString());
		return getAIDFromDF(sd);
	}

	private boolean assembleProcedure(){
		doWait((long) (SimulationConfig.SECONDS_TO_ASSEMBLE_FOR(assemblerType) * 1000));
		boolean broke = breakdownProcess();
		if(broke) return false;
		return true;
	}
}
