package agents.managers;

import agents.workers.machines.MachineAgent;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<MachineAgent, MachineType> {

	private AID supervisor;

	private Map<MachineType, MachineAgent> workingMachines = new HashMap<>();

	private Map<MachineType, List<MachineAgent>> spareMachines = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingMachines();
		setupSpareMachines();

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					System.out.println(msg.getContent());
				}
				block();
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
		List<MachineAgent> spareSolesMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Sole, spareSolesMachines);

		List<MachineAgent> spareCounterMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.DetailFabric, spareCounterMachines);

		List<MachineAgent> spareEyeletsMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.InnerFabric, spareEyeletsMachines);

		List<MachineAgent> spareTongueMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Outsole, spareTongueMachines);

		List<MachineAgent> spareUpperMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.SurfaceFabric, spareUpperMachines);
	}

	@Override
	public AID getSupervisor() {
		return supervisor;
	}

	@Override
	public Map<MachineType, MachineAgent> getWorkingMachines() {
		return workingMachines;
	}

	@Override
	public Map<MachineType, List<MachineAgent>> getSpareMachines() {
		return spareMachines;
	}

	private void startWorkerAgent(MachineType type) {
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.machines.MachineAgent", new Object[]{getAID()});
			ac.start();
			getWorkingMachines().put(type, null);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
