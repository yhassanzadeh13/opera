package simulator;


import metrics.Constants;
import metrics.Metrics;
import metrics.MetricsCollector;

public class SimulatorMetricsCollector {
  private static final String SUBSYSTEM_CHURN = "churn";
  private static final String NAMESPACE_SIMULATOR = "simulator";
  private static class Name {
    public static final String SESSION_LENGTH = "SessionLength";
    public static final String INTER_ARRIVAL = "InterArrival";
  }

  private static class HelpMsg {
    public static final String SESSION_LENGTH = "session length of nodes based on churn distribution";
    public static final String INTER_ARRIVAL = "inter arrival time of nodes based on churn distribution";
  }

  private static MetricsCollector metricsCollector;

  public SimulatorMetricsCollector(MetricsCollector metricsCollector) {
    SimulatorMetricsCollector.metricsCollector = metricsCollector;


    SimulatorMetricsCollector.metricsCollector.histogram().register(
        Name.SESSION_LENGTH,
        NAMESPACE_SIMULATOR,
        SUBSYSTEM_CHURN,
        HelpMsg.SESSION_LENGTH,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    SimulatorMetricsCollector.metricsCollector.histogram().register(
        Name.INTER_ARRIVAL,
        NAMESPACE_SIMULATOR,
        SUBSYSTEM_CHURN,
        HelpMsg.INTER_ARRIVAL,
        Constants.Histogram.DEFAULT_HISTOGRAM);
  }
}
