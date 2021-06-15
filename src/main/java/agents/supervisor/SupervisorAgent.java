package agents.supervisor;

import agents.events.Event;
import agents.events.EventType;
import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupervisorAgent extends Agent {

	private final List<ProductOrder> sentOrders = new ArrayList<>();

	private final List<ProductOrder> finishedOrders = new ArrayList<>();

	private AID machineManager;

	private AID assemblerManager;

	private AID simulationAgent;

	@Override
	protected void setup() {
		machineManager = startMachineManager();
		assemblerManager = startAssemblerManager();
		simulationAgent = startSimulationAgent();
		startDeadMachineContainer();

		doWait(2000);

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						if(msg.getProtocol().equals("FORDER")){
							Logger.supervisor("Supervisor has received a finished order");
							Event.createEvent(new Event(EventType.ORDER_COMPLETED, getAID(), getCurrentContainerName(), ""));

							ProductOrder finishedOrder = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							Optional<ProductOrder> sentOrder = sentOrders.stream()
									.filter(ord -> ord.getOrderId() == finishedOrder.getOrderId())
									.findFirst();

							if (sentOrder.isPresent()) {
								sentOrders.remove(sentOrder.get());
								finishedOrders.add(sentOrder.get());
								printFinishedOrders();
							}
						}else if(msg.getProtocol().equals("NORDER")){
							ProductOrder receivedOrder = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
							Logger.supervisor("Supervisor sends product plan to managers");

							ACLMessage msgToManagers = new ACLMessage(ACLMessage.INFORM);
							msgToManagers.setContent(JsonConverter.toJsonString(receivedOrder));
							msgToManagers.setProtocol("ORDER");
							msgToManagers.addReceiver(machineManager);
							msgToManagers.addReceiver(assemblerManager);
							send(msgToManagers);
							sentOrders.add(receivedOrder);
						}else if(msg.getProtocol().equals("STOGGL")){
							ACLMessage msgToToggleSimulation = new ACLMessage(ACLMessage.INFORM);
							msgToToggleSimulation.setProtocol("STOGGL");
							msgToToggleSimulation.addReceiver(simulationAgent);
							send(msgToToggleSimulation);
						}
					}
				}else{
					block();
				}
			}
		});
	}

	private AID startMachineManager() {
		try {
			ContainerController cc = getContainerController();
			AgentController ac = cc.createNewAgent("MachineManager", "agents.managers.MachineManager", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startAssemblerManager() {
		try {
			ContainerController cc = getContainerController();
			AgentController ac = cc.createNewAgent("AssemblerManager", "agents.managers.AssemblerManager", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private AID startSimulationAgent(){
		try {
			ContainerController cc = getContainerController();
			AgentController ac = cc.createNewAgent("SimulationAgent", "agents.simulation.SimulationAgent", new Object[]{getAID()});
			ac.start();
			return new AID(ac.getName(), AID.ISGUID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	private void printFinishedOrders() {
		Logger.supervisor("Prints list of finished orders:");
		for (ProductOrder order : finishedOrders) {
			String orderDescription = "";
			orderDescription += "Order: " + order.getOrderId() + "\n";
			orderDescription += "Product ID: " + order.getProductId() + "\n";
			orderDescription += "Product Amount: " + order.getProductAmount() + "\n";
			orderDescription += "Order Priority: " + order.getOrderPriority();
			Logger.summary(orderDescription, false);
		}
	}

	public void connectAssemblerManager(AID assemblerManager) {
		this.assemblerManager = assemblerManager;
	}

	public void connectMachineManager(AID machineManager) {
		this.machineManager = machineManager;
	}

	public AID getMachineManager() {
		return machineManager;
	}

	public AID getAssemblerManager() {
		return assemblerManager;
	}

	public List<ProductOrder> getSentOrders() {
		return sentOrders;
	}

	private void startDeadMachineContainer(){
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "DeadMachines");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		runtime.createAgentContainer(profile);
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
