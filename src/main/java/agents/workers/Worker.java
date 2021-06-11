package agents.workers;

import agents.configs.SimulationConfig;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import java.util.Date;
import java.util.Random;

public abstract class Worker<T> extends Agent {

	private static Date lastFailTime = new Date();

	private static int FAILURE_RATE = 7; // %

	private AID managerId;

	private String workerType;

    @Override
    protected void setup() {
		this.managerId = (AID) getArguments()[0];
		this.workerType = (String) getArguments()[1];
		setShuttingDownBehaviour();
    }

	private void setShuttingDownBehaviour() {
		addBehaviour(new TickerBehaviour(this, 5000) {
			@Override
			protected void onTick() {
				if (shouldFailNow()) {
					if (isNotBackupWorker()) {
						Logger.breaks(getLocalName() + (getLocalName().contains("Assembler") ? "" : "Machine") + " breaks...");

						sendUnfinishedTasToManager();
						deregisterIfAssembler(getAgent());
						doDelete();

						lastFailTime = new Date();
					}
				}
			}
		});
	}

    private void sendUnfinishedTasToManager() {
		var iAmDeadMessage = new ACLMessage();
		iAmDeadMessage.addReceiver(managerId);
		iAmDeadMessage.setContent(JsonConverter.toJsonString(getUnfinishedTask()));
		Logger.info(JsonConverter.toJsonString(getUnfinishedTask()));
		iAmDeadMessage.setPerformative(ACLMessage.CANCEL);
		send(iAmDeadMessage);
	}

	private boolean shouldFailNow() {
		Random random = new Random();
		return random.nextInt(100) < FAILURE_RATE && isReasonableTime();
	}

    private boolean isReasonableTime() {
    	return (new Date().getTime() - lastFailTime.getTime()) > (SimulationConfig.SECONDS_TO_NEXT_POSSIBLE_FAILURE * 1000);
	}

	private void deregisterIfAssembler(Agent agent) {
		try {
			if (getLocalName().contains("Assembler")) {
				DFService.deregister(agent);
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	private boolean isNotBackupWorker(){
		try {
			return !this.getContainerController().getContainerName().contains("Backup");
		} catch (ControllerException e) {
			e.printStackTrace();
			return false;
		}
	}

	public T getUnfinishedTask() {
		return null;
	}

	public AID getManagerId() {
		return managerId;
	}

	public String getWorkerType(){
    	return workerType;
	}
}
