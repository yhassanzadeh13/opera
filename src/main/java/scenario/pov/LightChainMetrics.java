package scenario.pov;

import java.util.HashMap;
import java.util.UUID;
import metrics.Constants;
import metrics.MetricsCollector;

public class LightChainMetrics {
  private final MetricsCollector metricsCollector;
  private final UUID collectorID;

  public LightChainMetrics(MetricsCollector metricsCollector){
    this.metricsCollector = metricsCollector;
    // We currently represent each time series by a UUID representing a node.
    // For LightChain however, we only monitor the overall progress of the system,
    // and not per node. Hence, we add a collector ID that represents the lable of the
    // sole time series for LightChain metrics.
    // TODO replace this with an option for registering metrics without label.
    this.collectorID = UUID.randomUUID();

    this.metricsCollector.counter().register(
        Constants.Demo.LightChain.Name.TRANSACTION_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TRANSACTION_COUNT
    );
    this.metricsCollector.counter().register(
        Constants.Demo.LightChain.Name.TOTAL_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TOTAL_BLOCKS_COUNT
    );
    this.metricsCollector.counter().register(
        Constants.Demo.LightChain.Name.TOTAL_UNIQUE_BLOCKS_COUNT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.TOTAL_UNIQUE_BLOCKS_COUNT
    );
    this.metricsCollector.gauge().register(
        Constants.Demo.LightChain.Name.CURRENT_BLOCK_HEIGHT,
        Constants.Namespace.DEMO,
        Constants.Demo.Subsystem.LightChain,
        Constants.Demo.LightChain.HelpMsg.CURRENT_BLOCK_HEIGHT
    );
  }

  public void OnNewTransactions(int count){
    this.metricsCollector.counter().inc(Constants.Demo.LightChain.Name.TRANSACTION_COUNT, this.collectorID, count);
  }

  public void OnNewBlock()


}
