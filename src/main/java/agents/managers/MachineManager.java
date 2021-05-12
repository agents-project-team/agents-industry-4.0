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
		getSpareMachines().put(MachineType.Counter, spareCounterMachines);

		List<Machine> spareEyeletsMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Eyelets, spareEyeletsMachines);

		List<Machine> spareTongueMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Tongue, spareTongueMachines);

		List<Machine> spareUpperMachines = new ArrayList<>();
		getSpareMachines().put(MachineType.Fabric, spareUpperMachines);
	}

	private void setupWorkingMachines() {
		getWorkingMachines().put(MachineType.Sole, null);
		getWorkingMachines().put(MachineType.Counter, null);
		getWorkingMachines().put(MachineType.Eyelets, null);
		getWorkingMachines().put(MachineType.Tongue, null);
		getWorkingMachines().put(MachineType.Fabric, null);
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
