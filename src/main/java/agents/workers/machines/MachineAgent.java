package agents.workers.machines;

import agents.workers.Worker;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

public class MachineAgent extends Agent implements Worker {
    private String customState;
    private Object bluePrint;
    private int numberOfDetails;
    private AID assemblerId;
    private int seconds;

    @Override
    protected void setup() {
        Behaviour onePartBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                block(seconds);
            }
        };
    }
}
