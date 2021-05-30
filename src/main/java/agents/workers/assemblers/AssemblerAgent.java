package agents.workers.assemblers;

import agents.product.PartPlan;
import agents.product.Product;
import agents.product.ProductPart;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.Worker;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblerAgent extends Worker<AssemblerState> {

	private Object blueprint;

	private AssemblerType assemblerType;

	private AID nextAssembler;

	private List<ProductPlan> currentPlans = new ArrayList<>();

	private List<ProductPart> storedParts = new ArrayList<>();

	@Override
	public void setup() {
		super.setup();
		setupWorkerCommunicationBehaviour();
	}

	private void setupWorkerCommunicationBehaviour() {
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						Logger.info(getLocalName() + " has received the plan!");

						if(!msg.getSender().getLocalName().equals("df")) {
							currentPlans.add(JsonConverter.fromJsonString(msg.getContent(), ProductPlan.class));
							currentPlans.sort(Comparator.comparing(ProductPlan::getPriority, Comparator.reverseOrder()));
						}
					} else if (msg.getPerformative() == ACLMessage.UNKNOWN) {
						if (msg.getProtocol().equals("SPART")) {
							ProductPart receivedPart = JsonConverter.fromJsonString(msg.getContent(), ProductPart.class);
							storedParts.add(receivedPart);
							assembleParts();
						}
					} else if (msg.getPerformative() == ACLMessage.REQUEST) {
						AssemblerState assemblerState = JsonConverter.fromJsonString(msg.getContent(), AssemblerState.class);
						currentPlans = assemblerState.getCurrentPlans();
						storedParts = assemblerState.getStoredParts();
						assembleParts();
					} else if (msg.getPerformative() == ACLMessage.PROPOSE) {
						Logger.process(getLocalName() + " replaces broken machine.");

						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);

						addAgentToRegistry();
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

	private void addAgentToRegistry() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		AssemblerType type = AssemblerType.getTypeByName(getWorkerType());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(type.toString());
		sd.setType(type.toString());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	private void assembleParts() {
		for (ProductPlan plan : currentPlans) {
			List<ProductPart> parts = getStorageParts(plan);
			if (parts.size() > 0) {
				Logger.info(getLocalName() + " is assembling part");

				doWait(2000);

				sendParts(parts);
				plan.decreaseAllAmounts();
			}
		}
	}

	private List<ProductPart> getStorageParts(ProductPlan plan) {
		List<ProductPart> partsToSend = new ArrayList<>();
		Set<ProductPart> partsStored = new HashSet<>(storedParts);
		Set<PartPlan> planProductParts = new HashSet<>(plan.getPlanParts().values());
		for (PartPlan partPlan : planProductParts) {
			for (ProductPart part : partsStored) {
				if (partPlan.getPartType().equals(part.getType())) {
					partsToSend.add(part);
					storedParts.remove(part);
					break;
				}
			}
		}
		if (!(partsToSend.size() == plan.getPlanParts().values().size())) {
			storedParts.addAll(partsToSend);
			partsToSend.clear();
		}
		return partsToSend;
	}

	private void sendParts(List<ProductPart> parts) {
		if (AssemblerType.getTypeByName(getWorkerType()) == AssemblerType.Final) {
			System.out.println("To manager");
			if (parts.size() > 0) {
				Product product = new Product(parts.get(0).getPartId(), 1, parts);
				ACLMessage msgToAssemblerManager = new ACLMessage(ACLMessage.UNKNOWN);
				msgToAssemblerManager.setProtocol("FPROD");
				msgToAssemblerManager.addReceiver(getManagerId());
				msgToAssemblerManager.setContent(JsonConverter.toJsonString(product));
				send(msgToAssemblerManager);
			}
		} else {
			System.out.println("To manager");
			AID receiverAID = getReceiverAID(AssemblerType.Final);
			for (ProductPart p : parts) {
				ACLMessage partsToFinalAssembler = new ACLMessage(ACLMessage.UNKNOWN);
				partsToFinalAssembler.setProtocol("SPART");
				partsToFinalAssembler.setContent(JsonConverter.toJsonString(p));
				partsToFinalAssembler.addReceiver(receiverAID);
				send(partsToFinalAssembler);
			}
		}
	}

	private AID getReceiverAID(AssemblerType type) {
		ServiceDescription sd = new ServiceDescription();
		sd.setName(type.toString());
		sd.setType(type.toString());
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, dfd);
			if (result.length > 0) {
				return result[0].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return null;
	}
}
