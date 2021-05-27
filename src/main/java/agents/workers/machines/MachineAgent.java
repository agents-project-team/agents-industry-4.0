package agents.workers.machines;

import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.utils.JsonConverter;
import agents.workers.Worker;
import agents.workers.assemblers.AssemblerType;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.crypto.Mac;

public class MachineAgent extends Worker<PartPlan> {

	private String customState;

	private Object bluePrint;

	private int numberOfDetails;

	private AID assemblerId;

	private int seconds;

	private MachineType ownType;

	private PartPlan currentPlan;

    @Override
    protected void setup() {
        super.setup();
		super.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						System.out.println("\n---------------- " + getLocalName() + " replaces broken machine. ----------------\n");
						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);
					} else if (msg.getPerformative() == ACLMessage.REQUEST) {
						System.out.println(getLocalName() + " machine has received a task!");
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						currentPlan = plan;

						//Processing time
						doWait(2000);

						ProductPart newPart = new ProductPart(plan.getPartType(), plan.getId());
						//Part is sent
						AID receiverAssembler = getCurrentAssembler();
						if(receiverAssembler!=null){
							ACLMessage msgToAssembler = new ACLMessage(ACLMessage.INFORM);
							msgToAssembler.addReceiver(receiverAssembler);
							msgToAssembler.setContent(JsonConverter.toJsonString(newPart));
							send(msgToAssembler);
						}
						//Return a message to the overlord
						ACLMessage msgToManager = new ACLMessage(ACLMessage.INFORM);
						msgToManager.addReceiver(getManagerId());
						msgToManager.setProtocol("FTASK");
						msgToManager.setContent(msg.getContent());
						send(msgToManager);
						System.out.println(getLocalName() + " machine has finished its work.");
					}
				} else {
					block();
				}
            }
		});
    }

	@Override
	public PartPlan getUnfinishedTask() {
		return currentPlan;
	}

	private AID getCurrentAssembler() {
		ServiceDescription sd = new ServiceDescription();
    	if(ownType == MachineType.DetailFabric || ownType == MachineType.SurfaceFabric || ownType == MachineType.InnerFabric){
    		sd.setType(AssemblerType.Fabric.toString());
		}else if(ownType == MachineType.Sole || ownType == MachineType.Outsole){
			sd.setType(AssemblerType.Sole.toString());
		}
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
