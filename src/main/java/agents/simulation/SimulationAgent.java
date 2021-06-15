package agents.simulation;

import agents.configs.SimulationConfig;
import agents.product.ProductOrder;
import agents.utils.JsonConverter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationAgent extends Agent {

    private static final Random random = new Random();

    private AID supervisor;

    @Override
    public void setup(){
        this.supervisor = (AID) getArguments()[0];
        setupGenerateOrderBehaviour();
    }

    private void setupGenerateOrderBehaviour(){
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                int timeTillNextOrder = generateRandomInt(SimulationConfig.MIN_SECONDS_PER_ORDER, SimulationConfig.MAX_SECONDS_PER_ORDER);

                doWait(timeTillNextOrder* 1000L);

                ProductOrder newOrder = new ProductOrder(
                        generateProductId(),
                        generateRandomInt(1, SimulationConfig.MAX_PRODUCT_AMOUNT),
                        generateRandomInt(1, SimulationConfig.MAX_PRIORITY_VALUE));

                ACLMessage orderMsg = new ACLMessage(ACLMessage.INFORM);
                orderMsg.setProtocol("NORDER");
                orderMsg.setContent(JsonConverter.toJsonString(newOrder));
                orderMsg.addReceiver(supervisor);
                send(orderMsg);
            }
        });
    }

    private int generateRandomInt(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }

    private String generatePartId(String type){
        StringBuilder idString = new StringBuilder(type);
        int amount = generateRandomInt(3,5);
        for(int i = 0; i < amount; i++){
            double numOrChar = random.nextDouble();
            if(numOrChar > 0.5){
                char charVal = (char)generateRandomInt(65,90);
                idString.append(String.valueOf(charVal));
            }else{
                idString.append(generateRandomInt(0, 9));
            }
        }
        return idString.toString();
    }

    private String generateProductId(){
        int initialCharNum = 65;
        StringBuilder productId = new StringBuilder();
        for(int i = 0; i < 5; i++){
            char initialChar = (char)(initialCharNum+i);
            int partAmount = generateRandomInt(1, SimulationConfig.MAX_AMOUNT_PER_PART);
            for(int j = 0; j < partAmount; j++){
                productId.append(generatePartId(String.valueOf(initialChar)));
                productId.append("-");
            }
        }
        productId.deleteCharAt(productId.length()-1);
        return productId.toString();
    }
}
