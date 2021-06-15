package agents.storage;

import agents.events.Event;
import agents.events.EventType;
import agents.product.Product;
import agents.utils.JsonConverter;
import agents.utils.Logger;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import java.util.ArrayList;
import java.util.List;

public class StorageAgent extends Agent {

    private final List<Product> productsStored = new ArrayList<>();


    @Override
    public void setup(){
        setupStorageCommunicationBehaviour();
    }

    private void setupStorageCommunicationBehaviour(){
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg != null){
                    if(msg.getPerformative() == ACLMessage.UNKNOWN){
                        if(msg.getProtocol().equals("FPRODS")){
                            Logger.info(getLocalName()+" has received a batch of products");

                            Product finishedProduct = JsonConverter.fromJsonString(msg.getContent(), Product.class);
                            Event.createEvent(new Event(EventType.PRODUCTS_RECEIVED, getAID(), getCurrentContainerName(), finishedProduct.toString()));
                            productsStored.add(finishedProduct);
                        }
                    }
                }else{
                    block();
                }
            }
        });
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
