package Utils;

import java.util.UUID;

/**
 * static class for providing various utils related to the simulator
 */
public class SimulatorUtils {

    public static String hashPairOfNodes(UUID a, UUID b){
        return a.toString() + b.toString();
    }
}
