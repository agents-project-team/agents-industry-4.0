package agents.workers;

import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import java.util.Random;

public abstract class Worker<T> extends Agent {

	private final static int FAILURE_RATE = 15; // %

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
				Random random = new Random();
				int randomNumber = random.nextInt(100);

				if (randomNumber < FAILURE_RATE) {
					try {
						String name = getAgent().getContainerController().getContainerName();
						if (!name.contains("Backup")) {
							System.out.println("\n================ " + getLocalName() + (getLocalName().contains("Assembler") ? "" :
									"Machine") + " breaks... ================\n");

							var iAmDeadMessage = new ACLMessage();
							iAmDeadMessage.addReceiver(managerId);
							iAmDeadMessage.setContent(JsonConverter.toJsonString(getUnfinishedTask()));
							iAmDeadMessage.setPerformative(ACLMessage.CANCEL);
							send(iAmDeadMessage);

							try {
								if (getLocalName().contains("Assembler")) {
									DFService.deregister(getAgent());
								}
							} catch (FIPAException e) {
							}

							doDelete();
						}
					} catch (ControllerException ignored) {
					}
				}
			}
		});
    }

	//    @Override
	//	protected void takeDown(){
	//    	try{
	//
	//		}catch(Exception e) { }
	//	}

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
