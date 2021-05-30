package agents.workers.machines;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum MachineType {
	Outsole, Sole, SurfaceFabric, DetailFabric, InnerFabric;

	public static MachineType getByName(String name) {
		return Arrays.stream(values())
				.filter(type -> name.contains(type.toString()))
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
	}
}
