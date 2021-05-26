package agents.workers.machines;

import agents.product.PartPlan;
import agents.product.ProductPart;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.Worker;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.TimeUnit;

public class MachineAgent extends Worker {

	private String customState;

	private Object bluePrint;

	private int numberOfDetails;

	private AID assemblerId;

	private int seconds;

    @Override
    protected void setup() {
        super.setup();
		super.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						System.out.println(getLocalName() + " replaces broken machine.");
						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);
					}
					if(msg.getPerformative() == ACLMessage.INFORM){
						System.out.println(this.getAgent().getAID()+" has received work!");
						PartPlan plan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						//Will be changed
						try {
							TimeUnit.SECONDS.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//Part is created
						ProductPart newPart = new ProductPart(plan.getPartType());
						//Part is sent
						ACLMessage msgToAssembler = new ACLMessage(ACLMessage.INFORM);
						msgToAssembler.addReceiver(assemblerId);
						msgToAssembler.setContent(JsonConverter.toJsonString(newPart));
						send(msgToAssembler);
						//Return a message to the overlord
						ACLMessage msgToManager = new ACLMessage();
						msgToManager.addReceiver(getManagerId());
						msgToManager.setContent("Done");
						send(msgToManager);
					}
				} else {
					block();
				}
            }
		});
    }
}
