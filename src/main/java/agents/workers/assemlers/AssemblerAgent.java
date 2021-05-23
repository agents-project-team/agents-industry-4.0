package agents.workers.assemlers;

import agents.workers.Worker;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.List;

public class AssemblerAgent extends Agent implements Worker {
    private AssemblerType assemblerType;
    private Object blueprint;
    private final ArrayList<Object> parts = new ArrayList<>();

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
