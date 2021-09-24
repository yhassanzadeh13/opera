package scenario.pov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import metrics.Constants;
import metrics.MetricsCollector;

public class LightChainMetrics {
  private static MetricsCollector metricsCollector;
  private static UUID collectorID;
  private static HashMap<Integer, List<UUID>> blockInventory;

  /***
   * Initializes LightChainMetrics collector.
   * @param metricsCollector an instance of metric collector. Supposed to be the same instance over all invocations by
   *                         different nodes. Though only the first invocation goes through.
   */
  public LightChainMetrics(MetricsCollector metricsCollector) {
    if(blockInventory != null){
      // already initialized
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
        Constants.Demo.LightChain.Name.TRANSACTION_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TRANSACTION_COUNT
    );
    LightChainMetrics.metricsCollector.counter().register(
        Constants.Demo.LightChain.Name.TOTAL_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TOTAL_BLOCKS_COUNT
    );
    LightChainMetrics.metricsCollector.counter().register(
        Constants.Demo.LightChain.Name.TOTAL_UNIQUE_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TOTAL_UNIQUE_BLOCKS_COUNT
    );
    LightChainMetrics.metricsCollector.gauge().register(
        Constants.Demo.LightChain.Name.CURRENT_BLOCK_HEIGHT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.CURRENT_BLOCK_HEIGHT
    );
  }

  /**
   * Is invoked whenever there is a new transaction created by a node, and increases the total number of transactions.
   * @param count number of transactions created.
   */
  public void OnNewTransactions(int count) {
    metricsCollector.counter().inc(Constants.Demo.LightChain.Name.TRANSACTION_COUNT, collectorID, count);
  }

  /**
   * OnNewBlock is invoked whenever a node creates a new finalized block, i.e., the node is the owner of that block. It
   * increments the total number of finalized blocks in the system.
   */
  private void OnNewBlock() {
    metricsCollector.counter().inc(Constants.Demo.LightChain.Name.TOTAL_BLOCKS_COUNT, collectorID);
  }

  /**
   * OnNewUniqueBlock is invoked whenever a node creates a new block, i.e., there is still no other blocks finalized at
   * the same height. It increments total number of unique blocks in the system. Note that unique blocks is an ephemeral
   * concept as other blocks at the same height may be finalized.
   */
  private void OnNewUniqueBlock() {
    metricsCollector.counter().inc(Constants.Demo.LightChain.Name.TOTAL_UNIQUE_BLOCKS_COUNT, collectorID);
  }

  /**
   * 
   * @param blockHeight
   * @param nodeID
   */
  private void OnBlockHeightUpdated(int blockHeight, UUID nodeID){
    metricsCollector.gauge().set(Constants.Demo.LightChain.Name.CURRENT_BLOCK_HEIGHT, nodeID, blockHeight);
  }

  public void OnNewBlock(int blockHeight, UUID blockID, UUID nodeID) {
    if (!blockInventory.containsKey(blockHeight)) {
      // there is no other block at this height yet
      // block is unique at this heigth
      blockInventory.put(blockHeight, new ArrayList<>(List.of(blockID)));
      this.OnNewBlock();
      this.OnNewUniqueBlock();

      return;
    }

    List<UUID> blockIDsAtHeight = blockInventory.get(blockHeight);
    if (blockIDsAtHeight.contains(blockID)) {
      // duplicate block
      return;
    }

    blockIDsAtHeight.add(blockID);
    blockInventory.put(blockHeight, blockIDsAtHeight);
    // block is not unique at this height
    this.OnNewBlock();
  }

}
