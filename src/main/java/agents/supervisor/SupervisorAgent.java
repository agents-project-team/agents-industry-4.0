package agents.supervisor;

import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.List;

public class SupervisorAgent extends Agent {

	private List<ProductOrder> receivedOrders = new ArrayList<>(
			List.of(new ProductOrder("AXX1-BXX2-CXX3-DXX4-EXX1", 3, 3),
					new ProductOrder("AXX1-BXX2-CXX3-DXX4-EXX1", 4, 4))
	);

	private List<ProductOrder> sentOrders = new ArrayList<>();

    private AID machineManager;

    private AID assemblerManager;

    @Override
    protected void setup() {
		machineManager = startMachineManager();
		assemblerManager = startAssemblerManager();

        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
				doWait(2000);
				if (receivedOrders.size() > 0) {
					ProductOrder order = receivedOrders.get(0);
					String productPlan = JsonConverter.toJsonString(order);

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent(productPlan);
					msg.setProtocol("ORDER");
					msg.addReceiver(machineManager);
					//msg.addReceiver(assemblerManager);
					System.out.println("Supervisor sent messages");
					send(msg);

                    receivedOrders.remove(order);
                    sentOrders.add(order);
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

    public void connectAssemblerManager(AID assemblerManager){
        this.assemblerManager = assemblerManager;
    }

    public void connectMachineManager(AID machineManager){
        this.machineManager = machineManager;
    }

    public void setReceivedOrders(List<ProductOrder> orders){
        receivedOrders = orders;
    }

    public void setSentOrders(List<ProductOrder> orders){
        sentOrders = orders;
    }

    public AID getMachineManager(){ return machineManager; }

	public AID getAssemblerManager(){ return assemblerManager ;}

	public List<ProductOrder> getReceivedOrders(){ return receivedOrders; }

	public List<ProductOrder> getSentOrders(){ return sentOrders; }
}
