package agents.managers;

import agents.product.Product;
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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AssemblerManager extends Agent implements Manager<AID, AssemblerType> {

	private AID supervisor;

	private final List<ProductOrder> currentOrders = new ArrayList<>();

	private final List<Product> finishedProducts = new ArrayList<>();

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
						if (msg.getProtocol().equals("ORDER")) {
							//Send the product plan to the assemblers
							ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							currentOrders.add(order);
							ProductPlan plan = new ProductPlan(order);
							sendPlanToFabricAssembler(plan);
							sendPlanToSoleAssembler(plan);
							sendPLanToFinalAssembler(plan);
						}
					} else if (msg.getPerformative() == ACLMessage.UNKNOWN) {
						if (msg.getProtocol().equals("FPROD")) {
							System.out.println(getLocalName()+" has received product");
							//Unpack product
							Product product = JsonConverter.fromJsonString(msg.getContent(), Product.class);
							//Store somewhere
							Optional<Product> addedProduct = finishedProducts.stream()
									.filter(p -> p.getProductId() == product.getProductId())
									.findFirst();
							if (addedProduct.isPresent()) {
								addedProduct.get().increaseAmount(1);
							} else {
								finishedProducts.add(product);
							}
							finishedProductsOperation();
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
					} else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						AID agentID = new AID(msg.getContent(), AID.ISGUID);
						addAgentToRegistry(agentID, Objects.requireNonNull(getKey(workingAssemblers, agentID)));
					}
				} else {
					block();
				}
			}
		});
	}

	private void setupSupervisor() {
		supervisor = (AID) getArguments()[0];
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
						startBackupAssemblerAgent(AssemblerType.Sole + "1", cc, AssemblerType.Sole),
						startBackupAssemblerAgent(AssemblerType.Sole + "2", cc, AssemblerType.Sole),
						startBackupAssemblerAgent(AssemblerType.Sole + "3", cc, AssemblerType.Sole)
				)
		));

		getSpareMachines().put(AssemblerType.Fabric, new ArrayList<>(
				Arrays.asList(
						startBackupAssemblerAgent(AssemblerType.Fabric + "1", cc, AssemblerType.Fabric),
						startBackupAssemblerAgent(AssemblerType.Fabric + "2", cc, AssemblerType.Fabric),
						startBackupAssemblerAgent(AssemblerType.Fabric + "3", cc,  AssemblerType.Fabric)
				)
		));

		getSpareMachines().put(AssemblerType.Final, new ArrayList<>(
				Arrays.asList(
						startBackupAssemblerAgent(AssemblerType.Final + "1", cc, AssemblerType.Final),
						startBackupAssemblerAgent(AssemblerType.Final + "2", cc, AssemblerType.Final),
						startBackupAssemblerAgent(AssemblerType.Final + "3", cc, AssemblerType.Final)
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
			AgentController ac = cc.createNewAgent("Assembler" + type.name(), "agents.workers.assemblers.AssemblerAgent", new Object[]{getAID(), type.toString()});
			ac.start();
			AID agentID = new AID(ac.getName(), AID.ISGUID);
			addAgentToRegistry(agentID, type);
			return agentID;
		} catch (StaleProxyException e) {
			throw new IllegalStateException();
		}
	}

	private AID startBackupAssemblerAgent(String name, ContainerController cc, AssemblerType type) {
		try {
			AgentController ac = cc.createNewAgent("AssemblerBackup" + name, "agents.workers.assemblers.AssemblerAgent", new Object[]{getAID(), type.toString()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private void sendPlanToFabricAssembler(ProductPlan plan) {
		ProductPlan fabricPlan = new ProductPlan(plan);
		Set<MachineType> machineTypes = new HashSet<>(fabricPlan.getPlanParts().keySet());
		for (MachineType key : machineTypes) {
			if (key == MachineType.Outsole || key == MachineType.Sole) {
				fabricPlan.getPlanParts().remove(key);
			}
		}
		ACLMessage msgToFabricAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToFabricAssembler.setContent(JsonConverter.toJsonString(fabricPlan));
		msgToFabricAssembler.addReceiver(workingAssemblers.get(AssemblerType.Fabric));
		send(msgToFabricAssembler);
	}

	private void sendPlanToSoleAssembler(ProductPlan plan) {
		ProductPlan solePlan = new ProductPlan(plan);
		Set<MachineType> machineTypes = new HashSet<>(solePlan.getPlanParts().keySet());
		for (MachineType key : machineTypes) {
			if (key == MachineType.DetailFabric || key == MachineType.SurfaceFabric || key == MachineType.InnerFabric) {
				solePlan.getPlanParts().remove(key);
			}
		}
		ACLMessage msgToSoleAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToSoleAssembler.setContent(JsonConverter.toJsonString(solePlan));
		msgToSoleAssembler.addReceiver(workingAssemblers.get(AssemblerType.Sole));
		send(msgToSoleAssembler);
	}

	private void sendPLanToFinalAssembler(ProductPlan plan) {
		ACLMessage msgToFinalAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToFinalAssembler.setContent(JsonConverter.toJsonString(plan));
		msgToFinalAssembler.addReceiver(workingAssemblers.get(AssemblerType.Final));
		send(msgToFinalAssembler);
	}

	private void finishedProductsOperation() {
		for (ProductOrder order : currentOrders) {
			Optional<Product> orderProduct = finishedProducts.stream()
					.filter(p -> p.getProductId() == order.getOrderId()).findFirst();
			if (orderProduct.isPresent()) {
				if (orderProduct.get().getProductAmount() >= order.getProductAmount()) {
					//Get Rid of products
					orderProduct.get().increaseAmount(-1 * order.getProductAmount());
					if(orderProduct.get().getProductAmount() == 0){
						finishedProducts.remove(order);
					}
					//Send message to supervisor
					notifyFinishedTask(order);
				}
			}
		}
	}

	private void notifyFinishedTask(ProductOrder order) {
		ACLMessage msgToSupervisor = new ACLMessage(ACLMessage.INFORM);
		msgToSupervisor.setProtocol("FORDER");
		msgToSupervisor.addReceiver(getSupervisor());
		msgToSupervisor.setContent(JsonConverter.toJsonString(order));
		send(msgToSupervisor);
		System.out.println(getLocalName()+" has sent a message to supervisor");
	}

	private void addAgentToRegistry(AID agent, AssemblerType type) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(agent);
		ServiceDescription sd = new ServiceDescription();
		sd.setName(type.toString());
		sd.setType(type.toString());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}
