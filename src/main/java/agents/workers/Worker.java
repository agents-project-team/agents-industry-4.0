package agents.workers;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import java.util.Random;

public abstract class Worker extends Agent {

	private final static int FAILURE_RATE = 15; // %

	private AID managerId;

    @Override
    protected void setup() {
		this.managerId = (AID) getArguments()[0];
		setShuttingDownBehaviour();
    }

	private void setShuttingDownBehaviour() {
		addBehaviour(new TickerBehaviour(this, 5000) {
			@Override
			protected void onTick() {
				Random random = new Random();
				int randomNumber = random.nextInt(100);

				if (randomNumber < FAILURE_RATE) {
					try {
						String name = getAgent().getContainerController().getContainerName();
						if (!name.contains("Backup")) {
							var iAmDeadMessage = new ACLMessage();
							iAmDeadMessage.addReceiver(managerId);
							iAmDeadMessage.setContent("I am dead " + getLocalName());
							iAmDeadMessage.setPerformative(ACLMessage.CANCEL);
							send(iAmDeadMessage);

							doDelete();
						}
					} catch (ControllerException ignored) {
					}
				}
			}
		});
    }

	public AID getManagerId() {
		return managerId;
	}
}
