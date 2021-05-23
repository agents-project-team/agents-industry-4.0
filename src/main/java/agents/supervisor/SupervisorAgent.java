package agents.supervisor;

import agents.product.ProductOrder;
import agents.product.ProductPlan;
import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class SupervisorAgent extends Agent {
    private List<ProductOrder> receivedOrders;
    private List<ProductOrder> sentOrders;
    private AID machineManager;
    private AID assemblerManager;

    @Override
    protected void setup() {
        System.out.println("Supervisor agent with id:" + this.getLocalName() + " has started!");
        receivedOrders = new ArrayList<>();
        sentOrders = new ArrayList<>();
        machineManager = null;
        assemblerManager = null;
        //Send Messages behaviour
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if(receivedOrders.size() > 0){
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
