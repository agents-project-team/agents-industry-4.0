package agents.workers.assemblers;

import agents.workers.Worker;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

public class AssemblerAgent extends Worker {

	private AssemblerType assemblerType;

	private Object blueprint;

	private final ArrayList<Object> parts = new ArrayList<>();

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
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						System.out.println(getLocalName() + " replaces broken machine.");
						ContainerID destination = new ContainerID();
						destination.setName("Main-Container");
						doMove(destination);
					} else if (msg.getPerformative() == ACLMessage.INFORM) {
						var content = msg.getContent();
						System.out.println(getLocalName() + " receives " + content);
					}
				} else {
					block();
				}
            }
		});
    }
}
