package scenario.finalita.metrics;

import metrics.Constants;
import metrics.opera.OperaCounter;
import metrics.opera.OperaGauge;
import metrics.opera.OperaHistogram;
import node.Identifier;

/**
 * Metrics collector for LightChain simulations.
 */
public class LightChainMetrics {
  private static final String SUBSYSTEM_LIGHTCHAIN = "lightchain";
  private final OperaCounter transactionCount;
  private final OperaGauge finalizedBlockHeightPerNode;
  private final OperaHistogram blockIdsPerHeight;

  /**
   * Initializes LightChainMetrics collector.
   */
  public LightChainMetrics() {
    this.transactionCount = new OperaCounter(LightChain.Name.TRANSACTION_COUNT,
            Constants.Namespace.DEMO,
            SUBSYSTEM_LIGHTCHAIN,
            LightChain.HelpMsg.TRANSACTION_COUNT);
    this.finalizedBlockHeightPerNode = new OperaGauge(LightChain.Name.CURRENT_BLOCK_HEIGHT,
            Constants.Namespace.DEMO,
            SUBSYSTEM_LIGHTCHAIN,
            LightChain.HelpMsg.CURRENT_BLOCK_HEIGHT,
            Constants.IDENTIFIER); // label
    this.blockIdsPerHeight = new OperaHistogram(LightChain.Name.BLOCK_IDS_PER_HEIGHT,
            Constants.Namespace.DEMO,
            SUBSYSTEM_LIGHTCHAIN,
            LightChain.HelpMsg.BLOCK_IDS_PER_HEIGHT,
            // Histogram keeps number of unique block ids per height, it can be as big as
            // the number of nodes in the network (one unique block id per node).
            // At this time it is far too big to go beyond 1000 nodes in an Opera simulation.
            // That is why we keep buckets sizes exponential to 2 up to 1024.
            new double[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024});
  }

  /**
   * Is invoked whenever there is a new transaction created by a node, and increases the total number of transactions.
   *
   * @param count number of transactions created.
   */
  public void onNewTransactions(int count) {
    this.transactionCount.increment(count);
  }

  /**
   * OnNewFinalizedBlock is invoked whenever a node creates a finalized block.
   * It keeps track of the total blocks as well as total unique block heights finalized in the system.
   * It also updates the telemetry view of node towards current finalized block height.
   *
   * @param blockHeight new finalized block height node generated.
   * @param blockId     identifier of the newly generated finalized block.
   * @param nodeId      identifier of node generating finalized block height.
   */
  public void onNewFinalizedBlock(int blockHeight, Identifier blockId, Identifier nodeId) {
    this.finalizedBlockHeightPerNode.set(nodeId, blockHeight);

    boolean unique = BlockInventory.getInstance().addBlock(blockHeight, blockId);
    if (unique) {
      // If the block is unique, we increment the total number of unique blocks per height in the system.
      this.blockIdsPerHeight.observe(BlockInventory.getInstance().getBlockCount(blockHeight));
    }
  }

  private static class LightChain {
    public static class Name {
      public static final String TRANSACTION_COUNT = "transaction_count";
      public static final String CURRENT_BLOCK_HEIGHT = "block_height";
      public static final String BLOCK_IDS_PER_HEIGHT = "total_finalized_blocks";
    }

    public static class HelpMsg {
      public static final String TRANSACTION_COUNT = "total number of transactions made in system";
      public static final String CURRENT_BLOCK_HEIGHT = "last finalized block height";
      public static final String BLOCK_IDS_PER_HEIGHT = "total finalized blocks";
    }
  }


}
