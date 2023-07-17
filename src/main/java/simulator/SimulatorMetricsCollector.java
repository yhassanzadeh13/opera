package simulator;


import metrics.Constants;
import metrics.opera.OperaGauge;
import metrics.opera.OperaHistogram;
import node.Identifier;

/**
 * SimulatorMetricsCollector is the metrics collector for the core simulator's functionalities.
 */
public class SimulatorMetricsCollector {
    private static final String SUBSYSTEM_CHURN = "churn";
    private static final String NAMESPACE_SIMULATOR = "simulator";
    private static final double[] CHURN_BUCKETS =
            new double[]{1, 100, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 1024000, 2048000, 4096000};

    private final OperaHistogram sessionLengthHistogram;
    private final OperaHistogram interarrivalTimeHistogram;
    private final OperaGauge onlineNodesCounter;
    private final OperaGauge offlineNodesCounter;

    /**
     * Creates a metric collector for core simulator functionalities.
     */
    public SimulatorMetricsCollector() {
        this.sessionLengthHistogram = new OperaHistogram(Name.SESSION_LENGTH,
                NAMESPACE_SIMULATOR,
                SUBSYSTEM_CHURN,
                HelpMsg.SESSION_LENGTH,
                CHURN_BUCKETS,
                Constants.IDENTIFIER);
        this.interarrivalTimeHistogram = new OperaHistogram(Name.INTER_ARRIVAL,
                NAMESPACE_SIMULATOR,
                SUBSYSTEM_CHURN,
                HelpMsg.INTER_ARRIVAL,
                CHURN_BUCKETS);
        this.onlineNodesCounter = new OperaGauge(Name.ONLINE_NODES,
                NAMESPACE_SIMULATOR,
                SUBSYSTEM_CHURN,
                HelpMsg.ONLINE_NODES);

        this.offlineNodesCounter = new OperaGauge(Name.OFFLINE_NODES,
                NAMESPACE_SIMULATOR,
                SUBSYSTEM_CHURN,
                HelpMsg.OFFLINE_NODES);

    }

    /**
     * Records the generated session length of the node in histogram. Session length is the online
     * duration of a node in the system.
     *
     * @param id            identifier of node.
     * @param sessionLength its session length.
     */
    public void onNewSessionLengthGenerated(Identifier id, double sessionLength) {
        this.sessionLengthHistogram.observe(id, sessionLength);
    }

    /**
     * Records the generated inter-arrival time of the node in histogram. Inter arrival time
     * is a global parameter of simulation denoting the time between two consecutive arrival of
     * nodes to the system.
     *
     * @param interArrival its inter arrival time.
     */
    public void onNewInterArrivalGenerated(double interArrival) {
        // Since inter arrival time is a global parameter, we record it by the collector id which
        // is a global identifier.
        this.interarrivalTimeHistogram.observe(interArrival);
    }

    /**
     * Updates the number of online nodes in the system.
     *
     * @param onlineNodes number of online nodes in the system.
     */
    public void updateOnlineNodes(int onlineNodes) {
        this.onlineNodesCounter.set(onlineNodes);
    }

    /**
     * Updates the number of offline nodes in the system.
     *
     * @param offlineNodes number of offline nodes in the system.
     */
    public void updateOfflineNodes(int offlineNodes) {
        this.offlineNodesCounter.set(offlineNodes);
    }

    private static class Name {
        public static final String SESSION_LENGTH = "session_length";
        public static final String INTER_ARRIVAL = "inter_arrival";
        public static final String ONLINE_NODES = "online_nodes";
        public static final String OFFLINE_NODES = "offline_nodes";
    }

    private static class HelpMsg {
        public static final String SESSION_LENGTH = "session length of nodes based on churn distribution";
        public static final String INTER_ARRIVAL = "inter arrival time of nodes based on churn distribution";
        public static final String ONLINE_NODES = "number of online nodes in the system";
        public static final String OFFLINE_NODES = "number of offline nodes in the system";
    }
}
