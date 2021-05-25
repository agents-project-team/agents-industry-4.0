package agents.workers.assemblers;

import agents.workers.Worker;
import jade.core.behaviours.Behaviour;
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
        var communicationBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                var receivedMessage = blockingReceive();
                var content = receivedMessage.getContent();
                System.out.println("Assembler " + getLocalName() + " " + content);
            }
        };
        addBehaviour(communicationBehaviour);
    }
}
