package agents.workers.assemblers;

import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.Worker;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AssemblerAgent extends Worker<Object> {

	private Object blueprint;

	private AssemblerType assemblerType;

	private AID nextAssembler;

	List<ProductPlan> currentPlans = new ArrayList<>();

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
					if(msg.getPerformative() == ACLMessage.INFORM){
						//Receive Plans from manager
						currentPlans.add(JsonConverter.fromJsonString(msg.getContent(), ProductPlan.class));
						currentPlans.sort(Comparator.comparing(ProductPlan::getPriority, Comparator.reverseOrder()));
					}else if(msg.getPerformative() == ACLMessage.UNKNOWN){
						if(msg.getProtocol().equals("SPART")){
							//Receive parts
							List<ProductPart> receivedParts = JsonConverter.fromJsonString(msg.getContent(), ArrayList.class);
							for(ProductPart part : receivedParts){
								storedParts.add(part);
							}
						}
					}else if (msg.getPerformative() == ACLMessage.PROPOSE) {
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
    			//Add waiting process
				sendParts(parts);
				plan.decreaseAllAmounts();
			}
		}
	}

	private List<ProductPart> getStorageParts(ProductPlan plan){
    	List<ProductPart> parts = new ArrayList<>();
    	for(PartPlan partPlan : plan.getPlanParts().values()){
    		for(ProductPart part : storedParts){
    			if(partPlan.getPartType().equals(part.getType())){
    				parts.add(part);
    				storedParts.remove(part);
    				break;
				}
			}
		}
    	if(!(parts.size() == plan.getPlanParts().values().size())){
			storedParts.addAll(parts);
		}
		return parts;
	}

	private void sendParts(List<ProductPart> parts){
    	if(assemblerType == AssemblerType.Fabric || assemblerType == AssemblerType.Sole){
    		ACLMessage partToFinalAssembler = new ACLMessage(ACLMessage.UNKNOWN);
    		partToFinalAssembler.setProtocol("SPART");
    		partToFinalAssembler.setContent(JsonConverter.toJsonString(parts));
    		//Add receiver / get receiever
			//send message
		}else if(assemblerType == AssemblerType.Final){
    		//Create full product
			System.out.println("Bro");
			//Send to manager
		}
	}
}
