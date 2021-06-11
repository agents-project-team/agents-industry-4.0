package agents.configs;

import agents.workers.assemblers.AssemblerType;
import java.util.Map;

public class SimulationConfig {

	public static final int SECONDS_TO_NEXT_POSSIBLE_FAILURE = 3;


	public static final int SECONDS_TO_CREATE_SURFACE_FABRIC = 3;

	public static final int   SECONDS_TO_CREATE_INNER_FABRIC = 1;

	public static final int  SECONDS_TO_CREATE_DETAIL_FABRIC = 2;

	public static final int		      SECONDS_TO_CREATE_SOLE = 4;

	public static final int 	   SECONDS_TO_CREATE_OUTSOLE = 1;


	private static final int SECONDS_TO_ASSEMBLE_FABRIC = 3;

	private static final int   SECONDS_TO_ASSEMBLE_SOLE = 2;

	private static final int  SECONDS_TO_ASSEMBLE_FINAL = 4;


	// ********************************************************************************************************************************************************


	private static Map<AssemblerType, Integer> assemblerTypeToSeconds = Map.of(
			AssemblerType.Fabric, SECONDS_TO_ASSEMBLE_FABRIC,
			AssemblerType.Sole,   SECONDS_TO_ASSEMBLE_SOLE,
			AssemblerType.Final,  SECONDS_TO_ASSEMBLE_FINAL
	);

	public static int SECONDS_TO_ASSEMBLE_FOR(AssemblerType assemblerType) {
		return assemblerTypeToSeconds.get(assemblerType);
	}

}
