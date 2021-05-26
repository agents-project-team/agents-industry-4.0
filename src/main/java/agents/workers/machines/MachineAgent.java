package agents.workers.machines;

import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.utils.JsonConverter;
import agents.workers.Worker;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

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
						System.out.println(getLocalName() + " machine has received a task!");
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						currentPlan = plan;

						//Processing time
						doWait(2000);

						ProductPart newPart = new ProductPart(plan.getPartType());

						//Part is sent
						ACLMessage msgToAssembler = new ACLMessage(ACLMessage.INFORM);
						msgToAssembler.addReceiver(assemblerId);
						msgToAssembler.setContent(JsonConverter.toJsonString(newPart));

						send(msgToAssembler);
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
}
