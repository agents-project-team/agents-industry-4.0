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
import java.util.ArrayList;
import java.util.List;

public class MachineAgent extends Worker<PartPlan> {

	private String customState;

	private Object bluePrint;

	private int numberOfDetails;

	private AID assemblerId;

	private int seconds;

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
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						currentPlan = plan;

						//Processing time
						System.out.println(getLocalName()+" is creating a part");
						doWait(2000);

						ProductPart createdPart = new ProductPart(plan.getPartType(), plan.getId());
						//Part is sent
						AID receiverAssembler = getCurrentAssembler();
						if(receiverAssembler!=null){
							ACLMessage msgToAssembler = new ACLMessage(ACLMessage.UNKNOWN);
							msgToAssembler.setProtocol("SPART");
							msgToAssembler.addReceiver(receiverAssembler);
							msgToAssembler.setContent(JsonConverter.toJsonString(createdPart));
							send(msgToAssembler);
						}
						//Return a message to the overlord
						ACLMessage msgToManager = new ACLMessage(ACLMessage.INFORM);
						msgToManager.addReceiver(getManagerId());
						msgToManager.setProtocol("FTASK");
						msgToManager.setContent(msg.getContent());
						send(msgToManager);
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
		String ownType = getWorkerType();
    	if(ownType.equals(MachineType.DetailFabric.toString()) || ownType.equals(MachineType.InnerFabric.toString()) || ownType.equals(MachineType.SurfaceFabric.toString())){
    		sd.setType(AssemblerType.Fabric.toString());
    		sd.setName(AssemblerType.Fabric.toString());
		}else if(ownType.equals(MachineType.Sole.toString()) || ownType.equals(MachineType.Outsole.toString())){
			sd.setType(AssemblerType.Sole.toString());
			sd.setName(AssemblerType.Sole.toString());
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
