package agents.supervisor;

import agents.product.ProductOrder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;

public class SupervisorAgent extends Agent {
    private List<ProductOrder> receivedOrders;
    private AID machineManager;
    private AID assemblerManager;

    @Override
    protected void setup() {
        System.out.println("Supervisor agent with id:" + this.getLocalName() + " has started!");
        receivedOrders = new ArrayList<>();
        machineManager = null;
        assemblerManager = null;

        //Receiving messages behaviour
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                ACLMessage msg = receive();
                if(msg != null){
                    //Parse Message
                    //Create new order
                    ProductOrder order = new ProductOrder("AXX1-BXX1-CXX1-DXX1-EXX1", 100, 1);
                    receivedOrders.add(order);
                }
            }
        });
    }

    public void connectAssemblerManager(AID assemblerManager){
        this.assemblerManager = assemblerManager;
    }

    public void connectMachineManager(AID machineManager){
        this.machineManager = machineManager;
    }
}
