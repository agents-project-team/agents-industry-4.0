package agents.workers;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class Worker extends Agent {
    protected volatile boolean isFailing = false;
    protected AID managerId;

    public Worker(AID managerId) {
        this.managerId = managerId;
    }

    @Override
    protected void setup() {
        setShuttingDownBehaviour();
    }

    protected void setShuttingDownBehaviour() {
        var shuttingDownBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
//                while (!isFailing) {
//                    Thread.onSpinWait();
//                }

                block(3000);

                var iAmDeadMessage = new ACLMessage();
                iAmDeadMessage.addReceiver(managerId);
                iAmDeadMessage.setContent("I am dead");
                iAmDeadMessage.setPerformative(ACLMessage.CANCEL);
                send(iAmDeadMessage);
            }
        };

        this.addBehaviour(shuttingDownBehaviour);
    }
}
