package agents.managers;

import agents.workers.assemblers.AssemblerType;
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

public class AssemblerManager extends Agent implements Manager<AID, AssemblerType> {

	private AID supervisor;

	private Map<AssemblerType, AID> workingAssemblers = new HashMap<>();

	private Map<AssemblerType, List<AID>> spareAssemblers = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupWorkingAssemblers();
		setupSpareAssemblers();

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

	private void setupWorkingAssemblers() {
		startAssemblerAgent(AssemblerType.Sole);
		startAssemblerAgent(AssemblerType.Final);
		startAssemblerAgent(AssemblerType.Fabric);

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

	private void setupSpareAssemblers() {
		List<AID> spareSolesAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Sole, spareSolesAssemblers);

		List<AID> spareCounterAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Fabric, spareCounterAssemblers);

		List<AID> spareFinalAssemblers = new ArrayList<>();
		getSpareMachines().put(AssemblerType.Final, spareFinalAssemblers);
	}

	@Override
	public AID getSupervisor() {
		return supervisor;
	}

	@Override
	public Map<AssemblerType, AID> getWorkingMachines() {
		return workingAssemblers;
	}

	@Override
	public Map<AssemblerType, List<AID>> getSpareMachines() {
		return spareAssemblers;
	}

	private void startAssemblerAgent(AssemblerType type) {
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent(type.name(), "agents.workers.assemblers.AssemblerAgent", new Object[]{getAID()});
			ac.start();
			getWorkingMachines().put(type, new AID(ac.getName(), AID.ISLOCALNAME));
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
