package agents.managers;

import agents.product.PartPlan;
import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.workers.assemblers.AssemblerType;
import agents.workers.machines.MachineType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.Arrays;
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
		setupBehaviours();
	}

	private void setupBehaviours() {
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						if(msg.getProtocol().equals("ORDER")){
							//Send plans to the assemblers accordingly
							ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							ProductPlan plan = new ProductPlan(order);
							sendPlanToFabricAssembler(plan);
							sendPlanToSoleAssembler(plan);
							sendPlanToFinalAssembler(plan);
						}else if(msg.getProtocol().equals("FTASK")){

						}
					} else if (msg.getPerformative() == ACLMessage.CANCEL) {
						AID deadMachine = msg.getSender();
						AssemblerType key = getKey(workingAssemblers, deadMachine);

						if (key != null) {
							var replacementMessage = new ACLMessage();
							if (spareAssemblers.get(key).isEmpty()) {
								System.out.println("No more " + key + " assemblers left.");
								return;
							}
							replacementMessage.addReceiver(spareAssemblers.get(key).get(0));
							replacementMessage.setPerformative(ACLMessage.PROPOSE);
							send(replacementMessage);

							workingAssemblers.computeIfPresent(key, (e, a) -> spareAssemblers.get(key).get(0));
							spareAssemblers.get(key).remove(0);
						}
					}
				} else {
					block();
				}
			}
		});
	}

	private void setupSupervisor() {
		supervisor = null;
	}

	private void setupWorkingAssemblers() {
		getWorkingMachines().put(AssemblerType.Sole, startAssemblerAgent(AssemblerType.Sole));
		getWorkingMachines().put(AssemblerType.Final, startAssemblerAgent(AssemblerType.Final));
		getWorkingMachines().put(AssemblerType.Fabric, startAssemblerAgent(AssemblerType.Fabric));
	}

	private void setupSpareAssemblers() {
		ContainerController cc = startBackupContainer();

		getSpareMachines().put(AssemblerType.Sole, new ArrayList<>(
				Arrays.asList(
						startBackupAssemblerAgent(AssemblerType.Sole + "1", cc),
						startBackupAssemblerAgent(AssemblerType.Sole + "2", cc),
						startBackupAssemblerAgent(AssemblerType.Sole + "3", cc)
				)
		));

		getSpareMachines().put(AssemblerType.Fabric, new ArrayList<>(
				Arrays.asList(
						startBackupAssemblerAgent(AssemblerType.Fabric + "1", cc),
						startBackupAssemblerAgent(AssemblerType.Fabric + "2", cc),
						startBackupAssemblerAgent(AssemblerType.Fabric + "3", cc)
				)
		));

		getSpareMachines().put(AssemblerType.Final, new ArrayList<>(
				Arrays.asList(
						startBackupAssemblerAgent(AssemblerType.Final + "1", cc),
						startBackupAssemblerAgent(AssemblerType.Final + "2", cc),
						startBackupAssemblerAgent(AssemblerType.Final + "3", cc)
				)
		));
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

	private ContainerController startBackupContainer() {
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "BackupAssemblers");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		return runtime.createAgentContainer(profile);
	}

	private static <K, V> K getKey(Map<K, V> map, V value) {
		for (K key : map.keySet()) {
			if (value.equals(map.get(key))) {
				return key;
			}
		}
		return null;
	}

	private AID startAssemblerAgent(AssemblerType type) {
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent("Assembler" + type.name(), "agents.workers.assemblers.AssemblerAgent", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (StaleProxyException e) {
			throw new IllegalStateException();
		}
	}

	private AID startBackupAssemblerAgent(String name, ContainerController cc) {
		try {
			AgentController ac = cc.createNewAgent("AssemblerBackup" + name, "agents.workers.assemblers.AssemblerAgent", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private void sendPlanToFabricAssembler(ProductPlan plan){
		List<PartPlan> partPlans = new ArrayList<>();
		partPlans.add(plan.getPlanParts().get(MachineType.SurfaceFabric));
		partPlans.add(plan.getPlanParts().get(MachineType.InnerFabric));
		partPlans.add(plan.getPlanParts().get(MachineType.DetailFabric));
		ACLMessage msgToFabricAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToFabricAssembler.addReceiver(workingAssemblers.get(AssemblerType.Fabric));
		msgToFabricAssembler.setContent(JsonConverter.toJsonString(partPlans));
		send(msgToFabricAssembler);
	}

	private void sendPlanToSoleAssembler(ProductPlan plan){
		List<PartPlan> partPlans = new ArrayList<>();
		partPlans.add(plan.getPlanParts().get(MachineType.Sole));
		partPlans.add(plan.getPlanParts().get(MachineType.Outsole));
		ACLMessage msgToSoleAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToSoleAssembler.addReceiver(workingAssemblers.get(AssemblerType.Sole));
		msgToSoleAssembler.setContent(JsonConverter.toJsonString(partPlans));
		send(msgToSoleAssembler);
	}

	private void sendPlanToFinalAssembler(ProductPlan plan){
		ACLMessage msgToFinalAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToFinalAssembler.addReceiver(workingAssemblers.get(AssemblerType.Final));
		msgToFinalAssembler.setContent(JsonConverter.toJsonString(plan));
		send(msgToFinalAssembler);
	}
}
