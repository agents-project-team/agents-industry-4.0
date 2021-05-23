package agents.workers.assemlers;

import agents.workers.Worker;
import java.util.ArrayList;

public class AssemblerAgent extends Worker {
    private AssemblerType assemblerType;
    private Object blueprint;
    private final ArrayList<Object> parts = new ArrayList<>();

	public AssemblerAgent() {
		super();
    }

    @Override
    public void setup() {
//        var configurationBehaviour = new CyclicBehaviour() {
//            @Override
//            public void action() {
//                // here should go
//                // - serialization of blueprint
//                // - configuration of the agent
//            }
//        };
//
//        addBehaviour(configurationBehaviour);
        super.setup();
    }
}
