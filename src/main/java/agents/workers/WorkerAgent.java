package agents.workers;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;

public class WorkerAgent extends Agent {
    private String customState;
    private String bluePrint;
    private int numberOfDetails;

    public String getCustomState() {
        return customState;
    }

    public void setCustomState(String customState) {
        this.customState = customState;
    }

    public int getNumberOfDetails() {
        return numberOfDetails;
    }

    public void setNumberOfDetails(int numberOfDetails) {
        this.numberOfDetails = numberOfDetails;
    }

    public String getBluePrint() {
        return bluePrint;
    }

    public void setBluePrint(String bluePrint) {
        this.bluePrint = bluePrint;
    }

    public WorkerAgent(String bluePrint) {
        this.bluePrint = bluePrint;
    }

    @Override
    protected void setup() {
        Behaviour onePartBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {

                block(3000);
            }
        };
    }
}
