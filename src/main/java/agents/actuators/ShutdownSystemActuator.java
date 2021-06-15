package agents.actuators;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ShutdownSystemActuator extends Agent {

    private AID supervisor;

    @Override
    public void setup(){
        supervisor = (AID) getArguments()[0];
        sendShutdownMsgBehaviour();
    }

    private void sendShutdownMsgBehaviour(){
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage shutdownMsg = new ACLMessage(ACLMessage.INFORM);
                shutdownMsg.setProtocol("SHUTDOWN");
                shutdownMsg.addReceiver(supervisor);
                send(shutdownMsg);
                doDelete();
            }
        });
    }
}
