package agents.configs;

import agents.workers.assemblers.AssemblerType;
import java.util.Map;

public class SimulationConfig {

	public static final int     FAILURE_RATE = 1;

	public static final boolean DISABLE_LOGS = false;

	// **************************************************************************************************************************************

	public static final double SECONDS_TO_NEXT_POSSIBLE_FAILURE = 0; // Be careful decreasing this, some concurrency issues may appear

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


	private static Map<AssemblerType, Double> assemblerTypeToSeconds = Map.of(
			AssemblerType.Fabric, SECONDS_TO_ASSEMBLE_FABRIC,
			AssemblerType.Sole,   SECONDS_TO_ASSEMBLE_SOLE,
			AssemblerType.Final,  SECONDS_TO_ASSEMBLE_FINAL
	);

	public static double SECONDS_TO_ASSEMBLE_FOR(AssemblerType assemblerType) {
		return assemblerTypeToSeconds.get(assemblerType);
	}

}
