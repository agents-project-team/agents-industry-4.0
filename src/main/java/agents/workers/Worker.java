package agents.workers;

import agents.configs.SimulationConfig;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Service;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import java.util.Date;
import java.util.Random;
import static agents.configs.SimulationConfig.FAILURE_RATE;

public abstract class Worker<T> extends Agent {

	private static Date lastFailTime = new Date();

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
//				if (shouldFailNow()) {
//					if (isNotBackupWorker()) {
//						Logger.breaks(getLocalName() + (getLocalName().contains("Assembler") ? "" : "Machine") + " breaks...");
//
//						sendUnfinishedTasToManager();
//						deregisterIfAssembler(getAgent());
//						doDelete();
//
//						lastFailTime = new Date();
//					}
//				}
			}
		});
	}

	public boolean breakdownProcess(){
    	if(shouldFailNow()){
    		sendUnfinishedTaskToManager();
    		deregisterAgent();
    		moveToDeadContainer();
    		return true;
		}
    	return false;
	}

    private void sendUnfinishedTaskToManager() {
		ACLMessage iAmDeadMessage = new ACLMessage(ACLMessage.CANCEL);
		iAmDeadMessage.addReceiver(managerId);
		iAmDeadMessage.setContent(JsonConverter.toJsonString(getUnfinishedTask()));
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

	public void registerAgent(String name){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(name+workerType);
		sd.setName(name+workerType);
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}catch(FIPAException fe){
			fe.printStackTrace();
		}
	}

	public void deregisterAgent(){
    	try{
    		DFService.deregister(this);
		}catch(FIPAException fe){
    		fe.printStackTrace();
		}
	}

	public AID getAIDFromDF(ServiceDescription sd){
    	DFAgentDescription dfd = new DFAgentDescription();
    	dfd.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, dfd);
			if (result.length > 0) {
				return result[0].getName();
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return null;
	}

	public void moveToMainContainer(){
    	ContainerID destination = new ContainerID();
    	destination.setName("Main-Container");
    	doMove(destination);
	}

	public void moveToDeadContainer(){
		ContainerID destination = new ContainerID();
		destination.setName("DeadMachines");
		doMove(destination);
	}

	protected void afterMove(){
    	AgentContainer ac = getContainerController();
    	String containerName = null;
		try {
			containerName = ac.getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		if(containerName != null){
			if(containerName.equals("Main-Container")) requestTaskFromManager();
		}
	}

	private void requestTaskFromManager(){
		//Request task
		ACLMessage msgRequestingTask = new ACLMessage(ACLMessage.INFORM);
		msgRequestingTask.setProtocol("RTASK");
		msgRequestingTask.setContent(workerType);
		msgRequestingTask.addReceiver(getManagerId());
		send(msgRequestingTask);
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
