package agents.managers;

import agents.events.Event;
import agents.events.EventType;
import agents.product.Product;
import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import agents.workers.assemblers.AssemblerState;
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
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssemblerManager extends Agent implements Manager<AID, AssemblerType> {

	private AID supervisor;

	private AID storageId;

	private final Map<AssemblerType, AssemblerState> unfinishedTasks = new HashMap<>();

	private final List<ProductOrder> currentOrders = new ArrayList<>();

	private final List<Product> finishedProducts = new ArrayList<>();

	private Map<AssemblerType, AID> workingAssemblers = new HashMap<>();

	private Map<AssemblerType, List<AID>> spareAssemblers = new HashMap<>();

	@Override
	protected void setup() {
		setupSupervisor();
		setupActiveAssemblers();
		setupSpareAssemblers();
		setupBehaviours();
		startStorageAgent();
	}

	private void setupBehaviours() {
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						if (msg.getProtocol().equals("ORDER")) {
							Logger.process("AssemblerManager distributes tasks among available machines");

							ProductOrder order = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							ProductPlan plan = new ProductPlan(order);
							sendPlanToFabricAssembler(plan);
							sendPlanToSoleAssembler(plan);
							sendPlanToFinalAssembler(plan);
							currentOrders.add(order);
						} else if (msg.getProtocol().equals("RTASK")){
							AssemblerType key = AssemblerType.getByName(msg.getContent());
							AssemblerState assemblerState = unfinishedTasks.get(key);
							Logger.process("Sending unfinished task to a backup " + key + " assembler");

							ACLMessage unfinishedPartMessage = new ACLMessage(ACLMessage.REQUEST);
							unfinishedPartMessage.addReceiver(workingAssemblers.get(key));
							unfinishedPartMessage.setContent(JsonConverter.toJsonString(assemblerState));
							send(unfinishedPartMessage);
							unfinishedTasks.remove(key);
						}
					} else if (msg.getPerformative() == ACLMessage.UNKNOWN) {
						if (msg.getProtocol().equals("FPROD")) {
							Logger.info(getLocalName() + " has received product");

							Product product = JsonConverter.fromJsonString(msg.getContent(), Product.class);
							System.out.println(product.toString());
							finishedProductsOperation(product);
						}
					} else if (msg.getPerformative() == ACLMessage.CANCEL) {
						AID deadMachine = msg.getSender();
						AssemblerType key = getKey(workingAssemblers, deadMachine);

						if(msg.getContent() == null || key == null) return;

						AssemblerState unfinishedAssemblerState = JsonConverter.fromJsonString(msg.getContent(), AssemblerState.class);
						unfinishedTasks.put(key, unfinishedAssemblerState);
						if (spareAssemblers.get(key).isEmpty()) {
							//Handler for no more assemblers
							Logger.info("No more " + key + " assemblers left.");
							return;
						}

						ACLMessage replacementMessage = new ACLMessage(ACLMessage.INFORM);
						replacementMessage.setProtocol("ACT");
						replacementMessage.addReceiver(spareAssemblers.get(key).get(0));
						send(replacementMessage);

						//Swap Machines
						AID replacementAID = spareAssemblers.get(key).get(0);
						AID oldAID = workingAssemblers.get(key);
						spareAssemblers.get(key).remove(0);
						workingAssemblers.replace(key, oldAID, replacementAID);
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

	private void setupActiveAssemblers() {
		int assemblerTypes = 3;
		ContainerController cc = getContainerController();
		for(int i = 0; i < assemblerTypes; i++){
			getActiveWorkers().put(AssemblerType.valueOf(i), startActiveAssemblerAgent(AssemblerType.valueOf(i)));
			Event.createEvent(new Event(EventType.AGENT_CREATED, getActiveWorkers().get(MachineType.valueOf(i)), getCurrentContainerName(), ""));
		}
	}

	private void setupSpareAssemblers() {
		ContainerController cc = getContainerController();
		int assemblerTypes = 3;
		int backupAmount = 3;
		for(int i = 0; i < assemblerTypes; i++){
			List<AID> tmpAssemblers = new ArrayList<>();
			for(int j = 1; j < backupAmount+1; j++){
				AID tmpBackupAssembler = startBackupAssemblerAgent(j, AssemblerType.valueOf(i), cc);
				tmpAssemblers.add(tmpBackupAssembler);
				Event.createEvent(new Event(EventType.AGENT_CREATED, tmpBackupAssembler, getCurrentContainerName(), ""));
			}
			getSpareWorkers().put(AssemblerType.valueOf(i), tmpAssemblers);
		}
	}

	@Override
	public AID getSupervisor() {
		return supervisor;
	}

	@Override
	public Map<AssemblerType, AID> getActiveWorkers() {
		return workingAssemblers;
	}

	@Override
	public Map<AssemblerType, List<AID>> getSpareWorkers() {
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

	private void startStorageAgent(){
		String name = "StorageAgent";
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent(name, "agents.storage.StorageAgent", new Object[]{});
			ac.start();
			AID agentID = new AID(ac.getName(), AID.ISGUID);
			storageId = agentID;
			Event.createEvent(new Event(EventType.AGENT_CREATED, agentID, getCurrentContainerName(), ""));
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	private AID startActiveAssemblerAgent(AssemblerType type) {
		ContainerController cc = getContainerController();
		try {
			AgentController ac = cc.createNewAgent("Assembler" + type.name(), "agents.workers.assemblers.AssemblerAgent",
					new Object[]{getAID(), type.toString()});
			ac.start();
			AID agentID = new AID(ac.getName(), AID.ISGUID);
			sendMsgToRegisterAgent(agentID);
			return agentID;
		} catch (StaleProxyException e) {
			throw new IllegalStateException();
		}
	}

	private AID startBackupAssemblerAgent(int backupNumber, AssemblerType type,  ContainerController cc) {
		String name = "BackupAssembler"+type.toString()+backupNumber;
		try {
			AgentController ac = cc.createNewAgent(name, "agents.workers.assemblers.AssemblerAgent",
					new Object[]{getAID(), type.toString()});
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
		msgToFabricAssembler.setProtocol("PPLAN");
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
		msgToSoleAssembler.setProtocol("PPLAN");
		msgToSoleAssembler.setContent(JsonConverter.toJsonString(solePlan));
		msgToSoleAssembler.addReceiver(workingAssemblers.get(AssemblerType.Sole));
		send(msgToSoleAssembler);
	}

	private void sendPlanToFinalAssembler(ProductPlan plan) {
		ACLMessage msgToFinalAssembler = new ACLMessage(ACLMessage.INFORM);
		msgToFinalAssembler.setProtocol("PPLAN");
		msgToFinalAssembler.setContent(JsonConverter.toJsonString(plan));
		msgToFinalAssembler.addReceiver(workingAssemblers.get(AssemblerType.Final));
		send(msgToFinalAssembler);
	}

	private void finishedProductsOperation(Product product) {
		//Add product or increase its amount in the list
		int index = getProductIndex(product.getProductId());
		if(index < 0){
			finishedProducts.add(product);
		}else{
			finishedProducts.get(index).increaseAmount(1);
		}
		index = getProductIndex(product.getProductId());
		int planIndex = getProductPlanIndex(product.getProductId());
		if(planIndex != -1) {
			if(finishedProducts.get(index).getProductAmount() == currentOrders.get(planIndex).getProductAmount()){
				//Send product and remove from plans(not implemented)
				System.out.println(currentOrders.get(planIndex).toString());
				System.out.println(finishedProducts.get(index).toString());
				notifyFinishedTask(currentOrders.get(planIndex));
				sendProductToStorage(finishedProducts.get(index));
				currentOrders.remove(planIndex);
				finishedProducts.remove(index);
			}
		}
	}

	private void notifyFinishedTask(ProductOrder order) {
		ACLMessage msgToSupervisor = new ACLMessage(ACLMessage.INFORM);
		msgToSupervisor.setProtocol("FORDER");
		msgToSupervisor.addReceiver(getSupervisor());
		msgToSupervisor.setContent(JsonConverter.toJsonString(order));
		send(msgToSupervisor);
		Logger.info(getLocalName() + " has sent a order to supervisor");
	}

	private void sendProductToStorage(Product finishedProducts){
		ACLMessage msgToStorage = new ACLMessage(ACLMessage.UNKNOWN);
		msgToStorage.setProtocol("FPRODS");
		msgToStorage.addReceiver(storageId);
		msgToStorage.setContent(JsonConverter.toJsonString(finishedProducts));
		send(msgToStorage);
		Logger.info(getLocalName()+" has sent finished products to storage");
	}

	private int getProductIndex(String newProdId){
		if(!finishedProducts.isEmpty()){
			int index = 0;
			for(Product finishedProduct : finishedProducts){
				if(finishedProduct.getProductId().equals(newProdId)) return index;
				index++;
			}
		}
		return -1;
	}

	private int getProductPlanIndex(String newProdId){
		if(!currentOrders.isEmpty()){
			int index = 0;
			for(ProductOrder productOrder : currentOrders){
				if(productOrder.getProductId().equals(newProdId)) return index;
				index++;
			}
		}
		return -1;
	}

	private void sendMsgToRegisterAgent(AID agentId){
		ACLMessage activateMsg = new ACLMessage(ACLMessage.INFORM);
		activateMsg.setProtocol("REG");
		activateMsg.addReceiver(agentId);
		send(activateMsg);
	}

	private String getCurrentContainerName(){
		ContainerController cc = getContainerController();
		String containerName = "";
		try {
			containerName = cc.getContainerName();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		return containerName;
	}
}
