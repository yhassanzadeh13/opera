package network.latency;

import java.util.HashMap;
import java.util.UUID;

import utils.SimulatorUtils;
import utils.generator.GaussianGenerator;

/**
 * Creates synthetic and symmetric latecy between nodes.
 */
public class LatencyGenerator {
  public static final int MeanLatency = 159;
  public static final int StdLatency = 96;
  private final HashMap<String, Integer> nodesSimulatedLatency;

  public LatencyGenerator() {
    this.nodesSimulatedLatency = new HashMap<>();
  }

  /**
   * get the simulated delay based on the normal distribution extracted from the AWS.
   *
   * @param nodeA         first node
   * @param nodeB         second node
   * @param bidirectional True, if simulated network.latency from A to B is the same as from B to A
   * @return new simulated network.latency
   */
  public int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional) {
    if (bidirectional && nodeA.compareTo(nodeB) < 0) {
      UUID tmp = nodeA;
      nodeA = nodeB;
      nodeB = tmp;
    }
    String hash = SimulatorUtils.hashPairOfNodes(nodeA, nodeB);
    if (!this.nodesSimulatedLatency.containsKey(hash)) {
      GaussianGenerator generator = new GaussianGenerator(MeanLatency, StdLatency);
      this.nodesSimulatedLatency.put(hash, Math.abs(generator.next()));
    }
    return this.nodesSimulatedLatency.get(hash);
  }
}
