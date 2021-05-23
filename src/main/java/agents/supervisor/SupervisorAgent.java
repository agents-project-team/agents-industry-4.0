package agents.supervisor;

import agents.product.ProductOrder;
import jade.core.AID;
import jade.core.Agent;

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
    }

    public void connectAssemblerManager(AID assemblerManager){
        this.assemblerManager = assemblerManager;
    }

    public void connectMachineManager(AID machineManager){
        this.machineManager = machineManager;
    }
}
