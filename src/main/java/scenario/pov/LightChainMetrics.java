package scenario.pov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import metrics.Constants;
import metrics.MetricsCollector;

/**
 * Metrics collector for LightChain simulations.
 */
public class LightChainMetrics {
  private static final ReentrantLock lock = new ReentrantLock();
  private static final String SUBSYSTEM_LIGHTCHAIN = "lightchain";
  private static MetricsCollector metricsCollector;
  private static UUID collectorID;
  private static HashMap<Integer, List<UUID>> blockInventory;

  /**
   * Initializes LightChainMetrics collector.
   *
   * @param metricsCollector an instance of metric collector. Supposed to be the same instance over all invocations by
   *                         different nodes. Though only the first invocation goes through.
   */
  public LightChainMetrics(MetricsCollector metricsCollector) {
    if (!lock.tryLock()) {
      // another thread is initiating
      return;
    }

    if (blockInventory != null) {
      // already initialized
      lock.unlock();
      return;
    }

    blockInventory = new HashMap<>();
    LightChainMetrics.metricsCollector = metricsCollector;


    // We currently represent each time series by a UUID representing a node.
    // For LightChain however, we only monitor the overall progress of the system,
    // and not per node. Hence, we add a collector ID that represents the lable of the
    // sole time series for LightChain metrics.
    // TODO replace this with an option for registering metrics without label.
    collectorID = UUID.randomUUID();

    LightChainMetrics.metricsCollector.counter().register(
        LightChain.Name.TRANSACTION_COUNT,
        Constants.Namespace.DEMO,
        SUBSYSTEM_LIGHTCHAIN,
        LightChain.HelpMsg.TRANSACTION_COUNT
    );
    LightChainMetrics.metricsCollector.counter().register(
        LightChain.Name.TOTAL_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        SUBSYSTEM_LIGHTCHAIN,
        LightChain.HelpMsg.TOTAL_BLOCKS_COUNT
    );
    LightChainMetrics.metricsCollector.counter().register(
        LightChain.Name.TOTAL_UNIQUE_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        SUBSYSTEM_LIGHTCHAIN,
        LightChain.HelpMsg.TOTAL_UNIQUE_BLOCKS_COUNT
    );
    LightChainMetrics.metricsCollector.gauge().register(
        LightChain.Name.CURRENT_BLOCK_HEIGHT,
        Constants.Namespace.DEMO,
        SUBSYSTEM_LIGHTCHAIN,
        LightChain.HelpMsg.CURRENT_BLOCK_HEIGHT
    );

    lock.unlock();
  }

  /**
   * Is invoked whenever there is a new transaction created by a node, and increases the total number of transactions.
   *
   * @param count number of transactions created.
   */
  public void onNewTransactions(int count) {
    metricsCollector.counter().inc(LightChain.Name.TRANSACTION_COUNT, collectorID, count);
  }

  /**
   * OnNewBlock is invoked whenever a node creates a new finalized block, i.e., the node is the owner of that block. It
   * increments the total number of finalized blocks in the system.
   */
  private void onNewFinalizedBlock() {
    metricsCollector.counter().inc(LightChain.Name.TOTAL_BLOCKS_COUNT, collectorID);
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
  public void onNewFinalizedBlock(int blockHeight, UUID blockId, UUID nodeId) {
    onBlockHeightUpdated(blockHeight, nodeId);

    if (!blockInventory.containsKey(blockHeight)) {
      // there is no other block at this height yet
      // block is unique at this height
      blockInventory.put(blockHeight, new ArrayList<>(List.of(blockId)));
      this.onNewFinalizedBlock();
      this.onNewUniqueBlock();

      return;
    }

    List<UUID> blockIdsAtHeight = blockInventory.get(blockHeight);
    if (blockIdsAtHeight.contains(blockId)) {
      // duplicate block
      return;
    }

    blockIdsAtHeight.add(blockId);
    blockInventory.put(blockHeight, blockIdsAtHeight);
    // block is not unique at this height
    this.onNewFinalizedBlock();
  }

  /**
   * OnNewUniqueBlock is invoked whenever a node creates a new block, i.e., there is still no other blocks finalized at
   * the same height. It increments total number of unique blocks in the system. Note that unique blocks is an ephemeral
   * concept as other blocks at the same height may be finalized.
   */
  private void onNewUniqueBlock() {
    metricsCollector.counter().inc(LightChain.Name.TOTAL_UNIQUE_BLOCKS_COUNT, collectorID);
  }

  /**
   * OnBlockHeightUpdated is invoked whenever the finalized block height on a node is progressing, i.e.,
   * by creating new finalized blocks.
   * It keeps track of the block height update per individual node.
   *
   * @param blockHeight new generated finalized block height
   * @param nodeId      the node identifier that generated the finalized block.
   */
  private void onBlockHeightUpdated(int blockHeight, UUID nodeId) {
    metricsCollector.gauge().set(LightChain.Name.CURRENT_BLOCK_HEIGHT, nodeId, blockHeight);
  }

  private static class LightChain {
    public class Name {
      public static final String TRANSACTION_COUNT = "transaction_count";
      public static final String CURRENT_BLOCK_HEIGHT = "block_height";
      public static final String TOTAL_BLOCKS_COUNT = "total_finalized_blocks";
      public static final String TOTAL_UNIQUE_BLOCKS_COUNT = "total_unique_finalized_blocks";
    }

    public class HelpMsg {
      public static final String TRANSACTION_COUNT = "total number of transactions made in system";
      public static final String CURRENT_BLOCK_HEIGHT = "last finalized block height";
      public static final String TOTAL_BLOCKS_COUNT = "total finalized blocks";
      public static final String TOTAL_UNIQUE_BLOCKS_COUNT = "total unique finalized blocks";
    }
  }


}
