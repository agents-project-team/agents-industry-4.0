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

	private List<ProductOrder> receivedOrders = new ArrayList<>();

	private List<ProductOrder> sentOrders = new ArrayList<>();
    private AID machineManager;
    private AID assemblerManager;

    @Override
    protected void setup() {
        System.out.println("Supervisor agent with id:" + this.getLocalName() + " has started!");

		machineManager = startMachineManager();
		assemblerManager = startAssemblerManager();

        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
				if (receivedOrders.size() > 0) {
					System.out.println("SEND TASK");
                    ProductOrder order = receivedOrders.get(0);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(machineManager);
                    msg.addReceiver(assemblerManager);
                    msg.setContent(JsonConverter.toJsonString(order));
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
			return new AID(ac.getName(), AID.ISLOCALNAME);
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
			return new AID(ac.getName(), AID.ISLOCALNAME);
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
