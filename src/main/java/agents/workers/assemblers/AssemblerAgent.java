package agents.workers.assemblers;

import agents.product.PartPlan;
import agents.product.Product;
import agents.product.ProductPart;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.Worker;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class AssemblerAgent extends Worker<Object> {

	private Object blueprint;

	private AssemblerType assemblerType;

	private AID nextAssembler;

	private final List<ProductPlan> currentPlans = new ArrayList<>();

	private final List<ProductPart> storedParts = new ArrayList<>();

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
					if(msg.getPerformative() == ACLMessage.INFORM){
						//Receive Plans from manager
						System.out.println(getLocalName()+" has received the plan!");
						currentPlans.add(JsonConverter.fromJsonString(msg.getContent(), ProductPlan.class));
						currentPlans.sort(Comparator.comparing(ProductPlan::getPriority, Comparator.reverseOrder()));
					}else if(msg.getPerformative() == ACLMessage.UNKNOWN){
						if(msg.getProtocol().equals("SPART")){
							//Receive parts
							ProductPart receivedPart = JsonConverter.fromJsonString(msg.getContent(), ProductPart.class);
							storedParts.add(receivedPart);
							assembleParts();
						}
					}else if (msg.getPerformative() == ACLMessage.PROPOSE) {
						ACLMessage msgToManager = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						msgToManager.setContent(JsonConverter.toJsonString(getAID()));
						msgToManager.addReceiver(getManagerId());
						send(msgToManager);
						System.out.println(getLocalName() + " replaces broken machine.");
						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);
					}
				} else {
					block();
				}
            }
		});
    }

    private void assembleParts(){
    	for(ProductPlan plan : currentPlans){
    		List<ProductPart> parts = getStorageParts(plan);
    		if(parts.size() > 0){
				System.out.println(getLocalName()+" is assembling part");

    			doWait(2000);

				sendParts(parts);
				plan.decreaseAllAmounts();
			}
		}
	}

	private List<ProductPart> getStorageParts(ProductPlan plan){
    	List<ProductPart> partsToSend = new ArrayList<>();
    	Set<ProductPart> partsStored = new HashSet<>(storedParts);
    	Set<PartPlan> planProductParts = new HashSet<>(plan.getPlanParts().values());
    	for(PartPlan partPlan : planProductParts){
    		for(ProductPart part : partsStored){
    			if(partPlan.getPartType().equals(part.getType())){
					partsToSend.add(part);
					storedParts.remove(part);
    				break;
				}
			}
		}
    	if(!(partsToSend.size() == plan.getPlanParts().values().size())){
			storedParts.addAll(partsToSend);
			partsToSend.removeAll(partsToSend);
		}
		return partsToSend;
	}

	private void sendParts(List<ProductPart> parts){
    	if(getWorkerType().equals(AssemblerType.Final.toString())){
			//Create full product
			if(parts.size() > 0){
				Product product = new Product(parts.get(0).getPartId(), 1, parts);
				ACLMessage msgToAssemblerManager = new ACLMessage(ACLMessage.UNKNOWN);
				msgToAssemblerManager.setProtocol("FPROD");
				msgToAssemblerManager.addReceiver(getManagerId());
				msgToAssemblerManager.setContent(JsonConverter.toJsonString(product));
				send(msgToAssemblerManager);
			}
		}else{
			AID receiverAID = getReceiverAID(AssemblerType.Final);
			for(ProductPart p : parts){
				ACLMessage partsToFinalAssembler = new ACLMessage(ACLMessage.UNKNOWN);
				partsToFinalAssembler.setProtocol("SPART");
				partsToFinalAssembler.setContent(JsonConverter.toJsonString(p));
				partsToFinalAssembler.addReceiver(receiverAID);
				send(partsToFinalAssembler);
			}
		}
	}

	private AID getReceiverAID(AssemblerType type){
		ServiceDescription sd = new ServiceDescription();
		sd.setName(type.toString());
		sd.setType(type.toString());
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try{
			DFAgentDescription[] result = DFService.search(this, dfd);
			if(result.length > 0){
				return result[0].getName();
			}
		}catch(FIPAException fe){
			fe.printStackTrace();
		}
		return null;
	}
}
