package agents.workers.assemblers;

import agents.workers.Worker;
import java.util.ArrayList;

public class AssemblerAgent extends Worker {
    private AssemblerType assemblerType;
    private Object blueprint;
    private final ArrayList<Object> parts = new ArrayList<>();

    @Override
    public void setup() {
        super.setup();
    }
}
