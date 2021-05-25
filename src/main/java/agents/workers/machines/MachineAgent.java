package agents.workers.machines;

import agents.workers.Worker;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class MachineAgent extends Worker {

	private String customState;

	private Object bluePrint;

	private int numberOfDetails;

	private AID assemblerId;

	private final int seconds = new Random().nextInt(6000);

    @Override
    protected void setup() {
        super.setup();


        Behaviour onePartBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = blockingReceive();
                String content = rcv.getContent();
                System.out.println("Worker " + getLocalName() + " " + content);
                block(seconds);
            }
        };
        addBehaviour(onePartBehaviour);
    }
}
