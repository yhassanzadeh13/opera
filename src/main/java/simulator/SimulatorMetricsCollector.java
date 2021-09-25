package simulator;


import metrics.MetricsCollector;

public class SimulatorMetricsCollector {
  public class Name {
    public static final String SESSION_LENGTH = "SessionLength";
    public static final String INTER_ARRIVAL = "InterArrival";
  }

  public class HelpMsg {
    public static final String SESSION_LENGTH = "session length of nodes based on churn distribution";
    public static final String INTER_ARRIVAL = "inter arrival time of nodes based on churn distribution";
  }

  public SimulatorMetricsCollector(MetricsCollector metricsCollector) {

  }
}
