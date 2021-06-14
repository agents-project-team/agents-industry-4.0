package agents.managers;

import agents.events.Event;
import agents.events.EventType;
import agents.product.PartPlan;
import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.machines.MachineType;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import java.util.*;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private AID supervisor;

	private final Map<MachineType, PartPlan> unfinishedTasks = new HashMap<>();

	private final List<ProductPlan> currentPlans = new ArrayList<>();

	private final Map<MachineType, AID> workingMachines = new HashMap<>();

	private final Map<MachineType, List<AID>> spareMachines = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupActiveMachines();
		setupSpareMachines();
		setupBehaviours();
	}

	//Sending the first task handling
	private void setupBehaviours() {
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						if (msg.getProtocol().equals("ORDER")) {
							Logger.process("MachineManager distributes tasks among available machines");

							ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							ProductPlan plan = new ProductPlan(order);
							for (AID worker : workingMachines.values()) {
								if (checkForMachineAvailability(getKey(workingMachines, worker))) {
									ACLMessage msgToWorker = new ACLMessage(ACLMessage.REQUEST);
									msgToWorker.addReceiver(worker);
									PartPlan partPlanToMachine = new PartPlan(plan.getPlanParts().get(getKey(workingMachines, worker)));
									partPlanToMachine.setCurrentAmount(1);
									msgToWorker.setContent(JsonConverter.toJsonString(partPlanToMachine));
									send(msgToWorker);
								}
							}
							addNewPlan(plan);
						} else if (msg.getProtocol().equals("FTASK")) {
							PartPlan responsePlan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
							MachineType key = getKey(workingMachines, msg.getSender());

							for (ProductPlan plan : currentPlans) {
								if (plan.getId() == responsePlan.getId()) {
									plan.decreasePartPlanAmount(key);
								}
							}
							currentPlans.removeIf(plan -> plan.getStatus() == PlanStatus.Completed);

							PartPlan highestPartPlan = getHighestPriorityPart(key);
							if (highestPartPlan != null) {
								assignTaskToMachine(highestPartPlan, msg.getSender());
							}
						} else if (msg.getProtocol().equals("RTASK")){
							MachineType key = MachineType.getByName(msg.getContent());
							PartPlan unfinishedPartPlan = unfinishedTasks.get(key);
							Logger.process("Sending unfinished task to a backup " + key + " machine");

							ACLMessage unfinishedPartMessage = new ACLMessage(ACLMessage.REQUEST);
							unfinishedPartMessage.addReceiver(workingMachines.get(key));
							unfinishedPartMessage.setContent(JsonConverter.toJsonString(unfinishedPartPlan));
							send(unfinishedPartMessage);
							unfinishedTasks.remove(key);
						}
					} else if (msg.getPerformative() == ACLMessage.CANCEL) {
						AID deadMachine = msg.getSender();
						MachineType key = getKey(workingMachines, deadMachine);

						if (msg.getContent() == null || key == null) return;

						PartPlan unfinishedPartPlan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						unfinishedTasks.put(key, unfinishedPartPlan);
						if (spareMachines.get(key).isEmpty()) {
							//Handler for no more machines
							System.out.println("No more " + key + " machines left.");
							return;
						}

						ACLMessage activateMessage = new ACLMessage(ACLMessage.INFORM);
						activateMessage.setProtocol("ACT");
						activateMessage.addReceiver(spareMachines.get(key).get(0));
						send(activateMessage);

						//Swap machines
						AID replacementAID = spareMachines.get(key).get(0);
						AID oldAID = workingMachines.get(key);
						spareMachines.get(key).remove(0);
						workingMachines.replace(key, oldAID, replacementAID);
					}
				} else {
					block();
				}
			}
		});
	}

	private void setupSupervisor() {
		supervisor = (AID) getArguments()[0];
	}

	private void setupActiveMachines() {
		int machineTypes = 5;
		ContainerController cc = getContainerController();
		String containerName = "";
		try {
			containerName = cc.getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < machineTypes; i++){
			getActiveWorkers().put(MachineType.valueOf(i), startActiveMachineAgent(MachineType.valueOf(i)));
			Event.createEvent(new Event(EventType.AGENT_CREATED, getActiveWorkers().get(MachineType.valueOf(i)), containerName, ""));
		}
	}

	private void setupSpareMachines() {
		int machineTypes = 5;
		int backupAmount = 3;
		ContainerController cc = startBackupContainer();
		String containerName = "";
		try {
			containerName = cc.getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < machineTypes; i++){
			List<AID> tmpMachines = new ArrayList<>();
			for(int j = 1; j < backupAmount+1; j++){
				AID tmpBackupMachine = startBackupMachineAgent(j, MachineType.valueOf(i), cc);
				tmpMachines.add(tmpBackupMachine);
				Event.createEvent(new Event(EventType.AGENT_CREATED, tmpBackupMachine, containerName, ""));
			}
			getSpareWorkers().put(MachineType.valueOf(i), tmpMachines);
		}
	}

	private ContainerController startBackupContainer() {
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "BackupMachines");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		return runtime.createAgentContainer(profile);
	}

	@Override
	public AID getSupervisor() {
		return supervisor;
	}

	@Override
	public Map<MachineType, AID> getActiveWorkers() {
		return workingMachines;
	}

	@Override
	public Map<MachineType, List<AID>> getSpareWorkers() {
		return spareMachines;
	}

	private AID startActiveMachineAgent(MachineType type) {
		try {
			ContainerController cc = getContainerController();
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.machines.MachineAgent", new Object[]{getAID(), type.toString()});
			ac.start();
			AID agentID = new AID(ac.getName(), AID.ISGUID);
			sendMsgToRegisterAgent(agentID);
			return agentID;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startBackupMachineAgent(int backupNumber, MachineType type, ContainerController cc) {
		String name = "Backup"+type.toString()+backupNumber;
		try {
			AgentController ac = cc.createNewAgent(name, "agents.workers.machines.MachineAgent", new Object[]{getAID(), type.toString()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private void assignTaskToMachine(PartPlan plan, AID machineID) {
		ACLMessage task = new ACLMessage(ACLMessage.REQUEST);
		task.addReceiver(machineID);
		task.setContent(JsonConverter.toJsonString(plan));
		send(task);
	}

	private static <K, V> K getKey(Map<K, V> map, V value) {
		for(K key: map.keySet()){
			if(value.equals(map.get(key))) return key;
		}
		return null;
	}

	private PartPlan getHighestPriorityPart(MachineType key){
		return currentPlans.stream()
				.map(plan -> plan.getPlanParts().get(key))
				.filter(part -> part.getCurrentAmount() > 0)
				.findFirst()
				.orElse(null);

	}

	private void addNewPlan(ProductPlan plan) {
		currentPlans.add(plan);
		currentPlans.sort(Comparator.comparing(ProductPlan::getPriority, Comparator.reverseOrder()));
	}

	private boolean checkForMachineAvailability(MachineType key){
		for(var p : currentPlans){
			PartPlan tmp = p.getPlanParts().get(key);
			if (tmp.getCurrentAmount() > 0) {
				return false;
			}
		}
		return true;
	}

	private void sendMsgToRegisterAgent(AID agentId){
		ACLMessage activateMsg = new ACLMessage(ACLMessage.INFORM);
		activateMsg.setProtocol("REG");
		activateMsg.addReceiver(agentId);
		send(activateMsg);
	}
}
