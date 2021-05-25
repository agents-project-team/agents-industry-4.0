package agents.managers;

import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private AID supervisor;

	private List<ProductPlan> currentPlans = new ArrayList<>();

	private Map<MachineType, AID> workingMachines = new HashMap<>();

	private Map<MachineType, List<AID>> spareMachines = new HashMap<>();

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
						ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
						ProductPlan plan = new ProductPlan(order);
						currentPlans.add(plan);

						ACLMessage msgToWorkers = new ACLMessage();
						for (AID worker : workingMachines.values()) {
							msgToWorkers.addReceiver(worker);
						}
						msgToWorkers.setContent(JsonConverter.toJsonString(plan));
						send(msgToWorkers);
					} else if (msg.getContent().equals("Done")) {
						//Handle decreasing product plan counter
						MachineType type = getKey(workingMachines, msg.getSender());
						ProductPlan highestPlan = getHighestPriorityPlan();
						assignTaskToMachine(highestPlan.getPlanParts().get(type), msg.getSender());
					} else if (msg.getPerformative() == ACLMessage.CANCEL) {
						AID deadMachine = msg.getSender();
						MachineType key = getKey(workingMachines, deadMachine);

						if (key != null) {
							var replacementMessage = new ACLMessage();
							if (spareMachines.get(key).isEmpty()) {
								System.out.println("No more " + key + " machines left.");
								return;
							}
							replacementMessage.addReceiver(spareMachines.get(key).get(0));
							replacementMessage.setPerformative(ACLMessage.PROPOSE);
							send(replacementMessage);

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
		getWorkingMachines().put(MachineType.Sole, startWorkerAgent(MachineType.Sole));
		getWorkingMachines().put(MachineType.DetailFabric, startWorkerAgent(MachineType.DetailFabric));
		getWorkingMachines().put(MachineType.InnerFabric, startWorkerAgent(MachineType.InnerFabric));
		getWorkingMachines().put(MachineType.Outsole, startWorkerAgent(MachineType.Outsole));
		getWorkingMachines().put(MachineType.SurfaceFabric, startWorkerAgent(MachineType.SurfaceFabric));
	}

	private void setupSpareMachines() {
		ContainerController cc = startBackupContainer();

		getSpareMachines().put(MachineType.Sole, new ArrayList<>(
				Arrays.asList(
						startBackupWorkerAgent(MachineType.Sole + "1", cc),
						startBackupWorkerAgent(MachineType.Sole + "2", cc),
						startBackupWorkerAgent(MachineType.Sole + "3", cc)
				)
		));

		getSpareMachines().put(MachineType.DetailFabric, new ArrayList<>(
				Arrays.asList(
						startBackupWorkerAgent(MachineType.DetailFabric + "1", cc),
						startBackupWorkerAgent(MachineType.DetailFabric + "2", cc),
						startBackupWorkerAgent(MachineType.DetailFabric + "3", cc)
				)
		));

		getSpareMachines().put(MachineType.InnerFabric, new ArrayList<>(
				Arrays.asList(
						startBackupWorkerAgent(MachineType.InnerFabric + "1", cc),
						startBackupWorkerAgent(MachineType.InnerFabric + "2", cc),
						startBackupWorkerAgent(MachineType.InnerFabric + "3", cc)
				)
		));

		getSpareMachines().put(MachineType.Outsole, new ArrayList<>(
				Arrays.asList(
						startBackupWorkerAgent(MachineType.Outsole + "1", cc),
						startBackupWorkerAgent(MachineType.Outsole + "2", cc),
						startBackupWorkerAgent(MachineType.Outsole + "3", cc)
				)
		));

		getSpareMachines().put(MachineType.SurfaceFabric, new ArrayList<>(
				Arrays.asList(
						startBackupWorkerAgent(MachineType.SurfaceFabric + "1", cc),
						startBackupWorkerAgent(MachineType.SurfaceFabric + "2", cc),
						startBackupWorkerAgent(MachineType.SurfaceFabric + "3", cc)
				)
		));
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
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.machines.MachineAgent", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startBackupWorkerAgent(String name, ContainerController cc) {
		try {
			AgentController ac = cc.createNewAgent("Backup" + name, "agents.workers.machines.MachineAgent", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private void assignTaskToMachine(String taskString, AID machineID){
		ACLMessage task = new ACLMessage(ACLMessage.INFORM);
		task.addReceiver(machineID);
		task.setContent(taskString);
		send(task);
	}

	private static <K, V> K getKey(Map<K, V> map, V value) {
		for(K key: map.keySet()){
			if(value.equals(map.get(key))) return key;
		}
		return null;
	}

	private ProductPlan getHighestPriorityPlan(){
		ProductPlan plan = null;
		int maxPriority = 0;
		for(var p : currentPlans){
			if(p.getPriority() > maxPriority){
				maxPriority = p.getPriority();
				plan = p;
			}
		}
		return plan;
	}
}
