package agents.managers;

import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.tools.sniffer.Message;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private List<ProductPlan> currentPlans;

	private AID supervisor;

	private Map<MachineType, AID> workingMachines = new HashMap<>();

	private Map<MachineType, List<AID>> spareMachines = new HashMap<>();

	@Override
	protected void setup() {
		currentPlans = new ArrayList<>();
		setupSupervisor();
		setupWorkingMachines();
		setupSpareMachines();

		//Sending the first task handling
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					//System.out.println(msg.getContent());
					if(msg.getSender() == supervisor){
						ProductPlan plan = JsonConverter.fromJsonString(msg.getContent(), ProductPlan.class);
						currentPlans.add(plan);
					}else{
						if(msg.getContent() == "Done"){
							//Handle decreasing product plan counter
							MachineType type = getKey(workingMachines, msg.getSender());
							ProductPlan highestPlan = getHighestPriorityPlan();
							assignTaskToMachine(highestPlan.getPlanParts().get(type), msg.getSender());
						}
					}
				}else{
					block();
				}
			}
		});
	}

	private void setupSupervisor() {
		supervisor = null;
	}

	private void setupWorkingMachines() {
		startWorkerAgent(MachineType.Sole);
		startWorkerAgent(MachineType.DetailFabric);
		startWorkerAgent(MachineType.InnerFabric);
		startWorkerAgent(MachineType.Outsole);
		startWorkerAgent(MachineType.SurfaceFabric);
	}

	private void setupSpareMachines() {
		List<AID> spareSolesMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Sole, spareSolesMachines);

		List<AID> spareCounterMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.DetailFabric, spareCounterMachines);

		List<AID> spareEyeletsMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.InnerFabric, spareEyeletsMachines);

		List<AID> spareTongueMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Outsole, spareTongueMachines);

		List<AID> spareUpperMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.SurfaceFabric, spareUpperMachines);
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

	private void startWorkerAgent(MachineType type) {
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.machines.MachineAgent", new Object[]{getAID()});
			ac.start();
			getWorkingMachines().put(type, new AID(ac.getName(), AID.ISLOCALNAME));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void assignTaskToMachine(String taskString, AID machineID){
		ACLMessage task = new ACLMessage(ACLMessage.INFORM);
		task.addReceiver(machineID);
		task.setContent(taskString);
		send(task);
	}

	public static <K, V> K getKey(Map<K,V> map, V value){
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
