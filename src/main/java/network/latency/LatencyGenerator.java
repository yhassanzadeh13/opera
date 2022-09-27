package network.latency;

import java.util.HashMap;

import node.Identifier;
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
   * get the simulated delay based on the normal distribution extracted from the AWS simulations.
   *
   * @param nodeA         first node
   * @param nodeB         second node
   * @param bidirectional True, if simulated latency from A to B is the same as from B to A, false otherwise.
   * @return new simulated latency.
   */
  public int getSimulatedLatency(Identifier nodeA, Identifier nodeB, boolean bidirectional) {
    if (bidirectional && nodeA.comparedTo(nodeB) < 0) {
      Identifier tmp = nodeA;
      nodeA = nodeB;
      nodeB = tmp;
    }
    String hash = concat(nodeA, nodeB);
    if (!this.nodesSimulatedLatency.containsKey(hash)) {
      GaussianGenerator generator = new GaussianGenerator(MeanLatency, StdLatency);
      this.nodesSimulatedLatency.put(hash, Math.abs(generator.next()));
    }
    return this.nodesSimulatedLatency.get(hash);
  }

  private static String concat(Identifier a, Identifier b) {
    return a.toString() + b.toString();
  }
}
