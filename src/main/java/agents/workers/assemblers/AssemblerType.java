package agents.workers.assemblers;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum AssemblerType {
	Fabric, Sole, Final;

	public static AssemblerType getTypeByName(String type) {
		return Arrays.stream(values())
				.filter(assemblerType -> assemblerType.toString().equals(type))
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
	}
}
