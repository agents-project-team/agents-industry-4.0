package agents.managers;

import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<AID, MachineType> {

	private AID supervisor;

	private Map<MachineType, AID> workingMachines = new HashMap<>();

	private Map<MachineType, List<AID>> spareMachines = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingMachines();
		setupSpareMachines();

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = blockingReceive();
				if (msg != null) {
					System.out.println(msg.getContent());
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
}
