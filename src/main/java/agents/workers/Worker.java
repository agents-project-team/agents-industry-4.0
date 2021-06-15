package agents.workers;

import agents.events.Event;
import agents.events.EventType;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import java.util.Random;

public abstract class Worker<T> extends Agent {

	private int failChanceIncremental = 0;

	private AID managerId;

	private String workerType;

    @Override
    protected void setup() {
		this.managerId = (AID) getArguments()[0];
		this.workerType = (String) getArguments()[1];
    }

	public boolean breakdownProcess(){
    	if(shouldFailNow()){
			Logger.breaks(getLocalName() + (getLocalName().contains("Assembler") ? "" : "Machine") + " breaks...");
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
		return (random.nextDouble() < calculateChance());
	}

	private double calculateChance(){
    	failChanceIncremental++;
    	double val = Math.exp((failChanceIncremental-7.9)/20)/100;
    	return val;
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

	public String getAgentCurrentContainerName(){
		ContainerController cc = getContainerController();
		String containerName = "";
		try {
			containerName = cc.getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		return containerName;
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
			if(containerName.equals("Main-Container")){
				requestTaskFromManager();
				Event.createEvent(new Event(EventType.AGENT_REPLACED, getAID(), getAgentCurrentContainerName(), ""));
			}
			if(containerName.equals("DeadMachines")){
				Event.createEvent(new Event(EventType.AGENT_DIED, getAID(), getAgentCurrentContainerName(), ""));
			}
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
