package agents.managers;

import agents.product.PartPlan;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private AID supervisor;

	private List<ProductPlan> currentPlans = new ArrayList<>();

	private List<ProductPlan> finishedPlans = new ArrayList<>();

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
						if (msg.getProtocol().equals("ORDER")) {
							//Add order to queue of orders
							ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							ProductPlan plan = new ProductPlan(order);
							//Check if contains orders, else send more
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
							addCurrentPlan(plan);
						} else if (msg.getProtocol().equals("FTASK")) {
							//Decrease amount functionality
							PartPlan responsePlan = JsonConverter.fromJsonString(msg.getContent(), PartPlan.class);
							MachineType key = getKey(workingMachines, msg.getSender());
							for (ProductPlan plan : currentPlans) {
								if (plan.getId() == responsePlan.getId()) {
									plan.decreasePartPlanAmount(key);
								}
							}
							PartPlan highestPartPlan = getHighestPriorityPart(key);
							if (highestPartPlan != null) {
								assignTaskToMachine(highestPartPlan, msg.getSender());
							}
						}
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

	private void assignTaskToMachine(PartPlan plan, AID machineID){
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

	private ProductPlan getHighestPriorityPlan() {
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

	private PartPlan getHighestPriorityPart(MachineType key){
		return currentPlans.stream()
				.map(plan -> plan.getPlanParts().get(key))
				.filter(part -> part.getCurrentAmount() > 0)
				.findFirst()
				.orElse(null);

		//		//Sorted list
		//		for(var p : currentPlans) {
		//			PartPlan tmp = p.getPlanParts().get(key);
		//			if(tmp.getCurrentAmount() > 0) return tmp;
		//		}
		//		return null;
	}

	private void addCurrentPlan(ProductPlan plan) {
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
