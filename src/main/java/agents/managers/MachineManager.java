package agents.managers;

import agents.product.PartPlan;
import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import javax.crypto.Mac;
import java.util.*;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private AID supervisor;

	private final List<ProductPlan> currentPlans = new ArrayList<>();

	private final Map<MachineType, AID> workingMachines = new HashMap<>();

	private final Map<MachineType, List<AID>> spareMachines = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingMachines();
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
						}
					} else if (msg.getPerformative() == ACLMessage.CANCEL) {
						AID deadMachine = msg.getSender();
						MachineType key = getKey(workingMachines, deadMachine);
						PartPlan unfinishedPartPlan = null;

						if (msg.getContent() != null) {
							unfinishedPartPlan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
						}

						if (key != null) {
							if (spareMachines.get(key).isEmpty()) {
								System.out.println("No more " + key + " machines left.");
								return;
							}

							var replacementMessage = new ACLMessage();
							replacementMessage.addReceiver(spareMachines.get(key).get(0));
							replacementMessage.setPerformative(ACLMessage.PROPOSE);
							send(replacementMessage);

							Logger.process("Sending unfinished task to a backup " + key + " machine");

							var unfinishedPartMessage = new ACLMessage();
							unfinishedPartMessage.addReceiver(spareMachines.get(key).get(0));
							unfinishedPartMessage.setContent(JsonConverter.toJsonString(unfinishedPartPlan));
							unfinishedPartMessage.setPerformative(ACLMessage.REQUEST);
							send(unfinishedPartMessage);

							workingMachines.computeIfPresent(key, (e, a) -> spareMachines.get(key).get(0));
							spareMachines.get(key).remove(0);
						}
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

	private void setupWorkingMachines() {
		int machineTypes = 5;
		for(int i = 0; i < machineTypes; i++){
			getWorkingMachines().put(MachineType.valueOf(i), startWorkerAgent(MachineType.valueOf(i)));
		}
	}

	private void setupSpareMachines() {
		ContainerController cc = startBackupContainer();
		int machineTypes = 5;
		int backupAmount = 3;
		for(int i = 0; i < machineTypes; i++){
			List<AID> tmpMachines = new ArrayList<>();
			for(int j = 1; j < backupAmount+1; j++){
				tmpMachines.add(startBackupWorkerAgent(j, MachineType.valueOf(i), cc));
			}
			getSpareMachines().put(MachineType.valueOf(i), tmpMachines);
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
	public Map<MachineType, AID> getWorkingMachines() {
		return workingMachines;
	}

	@Override
	public Map<MachineType, List<AID>> getSpareMachines() {
		return spareMachines;
	}

	private AID startWorkerAgent(MachineType type) {
		try {
			ContainerController cc = getContainerController();
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.machines.MachineAgent", new Object[]{getAID(), type.toString()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startBackupWorkerAgent(int backupNumber, MachineType type, ContainerController cc) {
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
}
