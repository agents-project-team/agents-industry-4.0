package agents.managers;

import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
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
		getWorkingMachines().put(MachineType.Sole, startWorkerAgent(MachineType.Sole));
		getWorkingMachines().put(MachineType.DetailFabric, startWorkerAgent(MachineType.DetailFabric));
		getWorkingMachines().put(MachineType.InnerFabric, startWorkerAgent(MachineType.InnerFabric));
		getWorkingMachines().put(MachineType.Outsole, startWorkerAgent(MachineType.Outsole));
		getWorkingMachines().put(MachineType.SurfaceFabric, startWorkerAgent(MachineType.SurfaceFabric));
	}

	private void setupSpareMachines() {
		ContainerController cc = startBackupContainer();

		getSpareMachines().put(MachineType.Sole, List.of(
				startBackupWorkerAgent(MachineType.Sole + "1", cc),
				startBackupWorkerAgent(MachineType.Sole + "2", cc)
		));

		getSpareMachines().put(MachineType.DetailFabric, List.of(
				startBackupWorkerAgent(MachineType.DetailFabric + "1", cc),
				startBackupWorkerAgent(MachineType.DetailFabric + "2", cc)
		));

		getSpareMachines().put(MachineType.InnerFabric, List.of(
				startBackupWorkerAgent(MachineType.InnerFabric + "1", cc),
				startBackupWorkerAgent(MachineType.InnerFabric + "2", cc)
		));

		getSpareMachines().put(MachineType.Outsole, List.of(
				startBackupWorkerAgent(MachineType.Outsole + "1", cc),
				startBackupWorkerAgent(MachineType.Outsole + "2", cc)
		));

		getSpareMachines().put(MachineType.SurfaceFabric, List.of(
				startBackupWorkerAgent(MachineType.SurfaceFabric + "1", cc),
				startBackupWorkerAgent(MachineType.SurfaceFabric + "2", cc)
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
			return new AID(ac.getName(), AID.ISLOCALNAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startBackupWorkerAgent(String name, ContainerController cc) {
		try {
			AgentController ac = cc.createNewAgent("Backup" + name, "agents.workers.machines.MachineAgent", new Object[]{getAID()});
			return new AID(ac.getName(), AID.ISLOCALNAME);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}
}
