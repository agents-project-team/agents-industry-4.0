package agents.managers;

import agents.workers.machines.MachineType;
import agents.workers.machines.MachineAgent;
import jade.core.AID;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<MachineAgent, MachineType> {

	private AID supervisor;

	private Map<MachineType, MachineAgent> workingMachines;

	private Map<MachineType, List<MachineAgent>> spareMachines;


	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingMachines();
		setupSpareMachines();
	}

	private void setupSupervisor() {
		supervisor = null;
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

	private void setupWorkingMachines() {
		getWorkingMachines().put(MachineType.Sole, null);
		getWorkingMachines().put(MachineType.DetailFabric, null);
		getWorkingMachines().put(MachineType.InnerFabric, null);
		getWorkingMachines().put(MachineType.Outsole, null);
		getWorkingMachines().put(MachineType.SurfaceFabric, null);
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
}
