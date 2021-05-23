package agents.managers;

import agents.workers.Machine;
import agents.workers.MachineType;
import jade.core.AID;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MachineManager extends Agent implements Manager<Machine, MachineType> {

	private AID supervisor;

	private Map<MachineType, Machine> workingMachines;

	private Map<MachineType, List<Machine>> spareMachines;


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
		List<Machine> spareSolesMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Sole, spareSolesMachines);

		List<Machine> spareCounterMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.DetailFabric, spareCounterMachines);

		List<Machine> spareEyeletsMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.InnerFabric, spareEyeletsMachines);

		List<Machine> spareTongueMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Outsole, spareTongueMachines);

		List<Machine> spareUpperMachines = new ArrayList<>();
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
	public Map<MachineType, Machine> getWorkingMachines() {
		return workingMachines;
	}

	@Override
	public Map<MachineType, List<Machine>> getSpareMachines() {
		return spareMachines;
	}
}
