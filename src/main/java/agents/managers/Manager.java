package agents.managers;

import jade.core.AID;
import java.util.List;
import java.util.Map;

public interface Manager<T, P> {

	AID getSupervisor();

	Map<P, T> getWorkingMachines();

	Map<P, List<T>> getSpareMachines();

}
