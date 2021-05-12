package agents.managers;

import agents.assemblers.Assembler;
import agents.assemblers.AssemblerType;
import jade.core.AID;
import jade.core.Agent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssemblerManager extends Agent implements Manager<Assembler, AssemblerType> {

	private AID supervisor;

	private Map<AssemblerType, Assembler> workingAssemblers;

	private Map<AssemblerType, List<Assembler>> spareAssemblers;


	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingAssemblers();
		setupSpareAssemblers();
	}

	private void setupSupervisor() {
		supervisor = null;
	}

	private void setupWorkingAssemblers() {
		getWorkingMachines().put(AssemblerType.Sole, null);
		getWorkingMachines().put(AssemblerType.Fabric, null);
		getWorkingMachines().put(AssemblerType.Final, null);
	}

	private void setupSpareAssemblers() {
		List<Assembler> spareSolesAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Sole, spareSolesAssemblers);

		List<Assembler> spareCounterAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Fabric, spareCounterAssemblers);

		List<Assembler> spareFinalAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Final, spareFinalAssemblers);
	}

	@Override
	public AID getSupervisor() {
		return supervisor;
	}

	@Override
	public Map<AssemblerType, Assembler> getWorkingMachines() {
		return workingAssemblers;
	}

	@Override
	public Map<AssemblerType, List<Assembler>> getSpareMachines() {
		return spareAssemblers;
	}
}
