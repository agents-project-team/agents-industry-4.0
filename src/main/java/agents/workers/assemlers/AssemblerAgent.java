package agents.workers.assemlers;

import agents.workers.Worker;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class AssemblerAgent extends Agent implements Worker {
    private AssemblerType assemblerType;
    private Object blueprint;

    public AssemblerAgent() {
        var configurationBehaviour = new CyclicBehaviour() {
            @Override
            public void action() {
                // here should go
                // - serialization of blueprint
                // - configuration of the agent
            }
        };

        addBehaviour(configurationBehaviour);
    }
}
