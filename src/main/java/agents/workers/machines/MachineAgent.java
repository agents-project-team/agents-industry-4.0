package agents.workers.machines;

import agents.workers.Worker;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

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
				} else {
					block();
				}
            }
		});
    }
}
