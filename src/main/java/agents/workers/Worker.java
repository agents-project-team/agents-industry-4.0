package agents.workers;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class Worker extends Agent {
    protected volatile boolean isFailing = false;
    protected AID managerId;

    @Override
    protected void setup() {
		this.managerId = (AID) getArguments()[0];
		setShuttingDownBehaviour();
    }

    protected void setShuttingDownBehaviour() {
        var shuttingDownBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {

				doWait(10000);

				var iAmDeadMessage = new ACLMessage();
                iAmDeadMessage.addReceiver(managerId);
                iAmDeadMessage.setContent("I am dead");
                iAmDeadMessage.setPerformative(ACLMessage.CANCEL);
                send(iAmDeadMessage);

				doDelete();
            }
        };

        this.addBehaviour(shuttingDownBehaviour);
    }
}
