package agents.workers.machines;

import agents.workers.Worker;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

public class MachineAgent extends Worker {
    private String customState;
    private Object bluePrint;
    private int numberOfDetails;
    private AID assemblerId;
    private int seconds;

    public MachineAgent(AID managerId) {
        super(managerId);
    }

    @Override
    protected void setup() {
        super.setup();
        Behaviour onePartBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                block(seconds);
            }
        };
    }
}
