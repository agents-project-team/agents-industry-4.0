package agents.workers.assemblers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum AssemblerType {
	Fabric(0),
	Sole(1),
	Final(2);

	private final int value;
	private static final Map map = new HashMap<>();

	AssemblerType(int value){
		this.value = value;
	}

	static{
		for(AssemblerType assemblerType : AssemblerType.values()){
			map.put(assemblerType.value, assemblerType);
		}
	}

	public static AssemblerType valueOf(int assemblerType){
		return (AssemblerType) map.get(assemblerType);
	}

	public int getValue(){
		return this.value;
	}

	public static AssemblerType getTypeByName(String type) {
		return Arrays.stream(values())
				.filter(assemblerType -> assemblerType.toString().equals(type))
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
	}
}
