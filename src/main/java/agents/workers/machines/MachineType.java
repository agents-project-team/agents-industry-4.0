package agents.workers.machines;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum MachineType {
	Outsole(0),
	Sole(1),
	SurfaceFabric(2),
	DetailFabric(3),
	InnerFabric(4);

	private final int value;
	private static final Map map = new HashMap<>();

	MachineType(int value){
		this.value = value;
	}

	static{
		for(MachineType machineType : MachineType.values()){
			map.put(machineType.value, machineType);
		}
	}

	public static MachineType valueOf(int machineType){
		return (MachineType) map.get(machineType);
	}

	public int getValue(){
		return this.value;
	}

	public static MachineType getByName(String name) {
		return Arrays.stream(values())
				.filter(type -> name.contains(type.toString()))
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
	}
}
