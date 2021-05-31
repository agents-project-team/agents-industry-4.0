package agents.supervisor;

import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupervisorAgent extends Agent {

	private List<ProductOrder> receivedOrders = new ArrayList<>(
			List.of(new ProductOrder("AXX1-BXX2-CXX3-DXX4-EXX1", 2, 3),
					new ProductOrder("AXX1-BXX2-CXX3-DXX4-EXX1", 3, 3))
	);

	private List<ProductOrder> sentOrders = new ArrayList<>();

	private List<ProductOrder> finishedOrders = new ArrayList<>();

	private AID machineManager;

	private AID assemblerManager;

	@Override
	protected void setup() {
		machineManager = startMachineManager();
		assemblerManager = startAssemblerManager();

		doWait(2000);

		addBehaviour(new TickerBehaviour(this, 2000) {
			@Override
			protected void onTick() {
				ACLMessage msg = receive();
				if (receivedOrders.size() > 0) {
					Logger.supervisor("Supervisor sends product plan to managers");

					ProductOrder order = receivedOrders.get(0);
					String productPlan = JsonConverter.toJsonString(order);

					ACLMessage msgToManagers = new ACLMessage(ACLMessage.INFORM);
					msgToManagers.setContent(productPlan);
					msgToManagers.setProtocol("ORDER");
					msgToManagers.addReceiver(machineManager);
					msgToManagers.addReceiver(assemblerManager);
					send(msgToManagers);

					receivedOrders.remove(order);
					sentOrders.add(order);
				}
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM && msg.getProtocol().equals("FORDER")) {
						Logger.supervisor("Supervisor has received a finished order");

						ProductOrder finishedOrder = JsonConverter.fromJsonString(msg.getContent(), ProductOrder.class);
						Optional<ProductOrder> sentOrder = sentOrders.stream()
								.filter(ord -> ord.getOrderId() == finishedOrder.getOrderId())
								.findFirst();
						if (sentOrder.isPresent()) {
							sentOrders.remove(sentOrder.get());
							finishedOrders.add(sentOrder.get());
							printFinishedOrders();
						}
						if (sentOrders.size() == 0) {
							Logger.summary("All orders have been completed", true);
							System.exit(0);
						}
					}
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

	public void setReceivedOrders(List<ProductOrder> orders) {
		receivedOrders = orders;
	}

	public void setSentOrders(List<ProductOrder> orders) {
		sentOrders = orders;
	}

	public AID getMachineManager() {
		return machineManager;
	}

	public AID getAssemblerManager() {
		return assemblerManager;
	}

	public List<ProductOrder> getReceivedOrders() {
		return receivedOrders;
	}

	public List<ProductOrder> getSentOrders() {
		return sentOrders;
	}
}
