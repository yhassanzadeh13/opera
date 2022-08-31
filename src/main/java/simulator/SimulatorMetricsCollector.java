package simulator;


import java.util.UUID;

import metrics.Constants;
import metrics.MetricsCollector;

/**
 * SimulatorMetricsCollector is the metrics collector for the core simulator's functionalities.
 */
public class SimulatorMetricsCollector {
  private static final String SUBSYSTEM_CHURN = "churn";
  private static final String NAMESPACE_SIMULATOR = "simulator";
  private static final UUID collectorID = UUID.randomUUID();
  private MetricsCollector metricsCollector;

  /**
   * Creates a metric collector for core simulator functionalities.
   *
   * @param metricsCollector root metric collector of opera.
   */
  public SimulatorMetricsCollector(MetricsCollector metricsCollector) {
    this.metricsCollector = metricsCollector;

    this.metricsCollector.histogram().register(
        Name.SESSION_LENGTH,
        NAMESPACE_SIMULATOR,
        SUBSYSTEM_CHURN,
        HelpMsg.SESSION_LENGTH,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    this.metricsCollector.histogram().register(
        Name.INTER_ARRIVAL,
        NAMESPACE_SIMULATOR,
        SUBSYSTEM_CHURN,
        HelpMsg.INTER_ARRIVAL,
        Constants.Histogram.DEFAULT_HISTOGRAM);
  }

  /**
   * Records the generated session length of the node in histogram. Session length is the online
   * duration of a node in the system.
   *
   * @param id            identifier of node.
   * @param sessionLength its session length.
   */
  public void onNewSessionLengthGenerated(UUID id, int sessionLength) {
    this.metricsCollector.histogram().observe(Name.SESSION_LENGTH, id, sessionLength);
  }

  /**
   * Records the generated inter-arrival time of the node in histogram. Inter arrival time
   * is a global parameter of simulation denoting the time between two consecutive arrival of
   * nodes to the system.
   *
   * @param interArrival its inter arrival time.
   */
  public void onNewInterArrivalGenerated(int interArrival) {
    // Since inter arrival time is a global parameter, we record it by the collector id which
    // is a global identifier.
    this.metricsCollector.histogram().observe(Name.INTER_ARRIVAL, collectorID, interArrival);
  }

  private static class Name {
    public static final String SESSION_LENGTH = "session_length";
    public static final String INTER_ARRIVAL = "inter_arrival";
  }

  private static class HelpMsg {
    public static final String SESSION_LENGTH = "session length of nodes based on churn distribution";
    public static final String INTER_ARRIVAL = "inter arrival time of nodes based on churn distribution";
  }
}
