package agents.configs;

import agents.workers.assemblers.AssemblerType;
import java.util.Map;

public class SimulationConfig {

	public static final boolean DISABLE_LOGS = false;

	// **************************************************************************************************************************************

	public static final int MachineTypesAmount = 5;

	public static final int AssemblerTypesAmount  = 3;

	public static final int BackupAssemblerAmount = 10;

	public static final int BackupMachineAmount = 10;

	// **************************************************************************************************************************************

	public static final double SECONDS_TO_CREATE_SURFACE_FABRIC = 3.01;

	public static final double   SECONDS_TO_CREATE_INNER_FABRIC = 1.12;

	public static final double  SECONDS_TO_CREATE_DETAIL_FABRIC = 2.33;

	public static final double		     SECONDS_TO_CREATE_SOLE = 3.84;

	public static final double 	      SECONDS_TO_CREATE_OUTSOLE = 1.12;

	// **************************************************************************************************************************************

	private static final double SECONDS_TO_ASSEMBLE_FABRIC = 3.19;

	private static final double   SECONDS_TO_ASSEMBLE_SOLE = 2.67;

	private static final double  SECONDS_TO_ASSEMBLE_FINAL = 4.24;

	// **************************************************************************************************************************************

	public static final int MAX_PRODUCT_AMOUNT = 10;

	public static final int MAX_AMOUNT_PER_PART = 1;

	public static final int MAX_PRIORITY_VALUE = 1000;

	// **************************************************************************************************************************************

	public static final int MAX_SECONDS_PER_ORDER = 10;

	public static final int MIN_SECONDS_PER_ORDER = 10;

	private static Map<AssemblerType, Double> assemblerTypeToSeconds = Map.of(
			AssemblerType.Fabric, SECONDS_TO_ASSEMBLE_FABRIC,
			AssemblerType.Sole,   SECONDS_TO_ASSEMBLE_SOLE,
			AssemblerType.Final,  SECONDS_TO_ASSEMBLE_FINAL
	);

	public static double SECONDS_TO_ASSEMBLE_FOR(AssemblerType assemblerType) {
		return assemblerTypeToSeconds.get(assemblerType);
	}

}
