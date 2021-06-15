package agents.actuators;

import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class CreateOrderActuator extends Agent {

    private ProductOrder orderToSend;

    private AID supervisor;

    @Override
    public void setup(){
        supervisor = (AID) getArguments()[0];
        String orderString = (String) getArguments()[1];
        orderToSend = JsonConverter.fromJsonString(orderString, ProductOrder.class);
        sendOrderBehaviour();
    }

    public void sendOrderBehaviour(){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage sendOrderMsg = new ACLMessage(ACLMessage.INFORM);
                sendOrderMsg.setProtocol("NORDER");
                sendOrderMsg.setContent(JsonConverter.toJsonString(orderToSend));
                sendOrderMsg.addReceiver(supervisor);
                send(sendOrderMsg);
                doDelete();
            }
        });
    }
}
