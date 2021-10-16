package underlay;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import metrics.Constants;
import metrics.MetricsCollector;

public class MiddleLayerMetricsCollector {
  public static final String SUBSYSTEM_MIDDLELAYER = "middlelayer";
  private static final ReentrantLock lock = new ReentrantLock();
  public static final String PACKET_SIZE = "packet_size";
  private static MetricsCollector metricsCollector;

  public MiddleLayerMetricsCollector(MetricsCollector metricsCollector) {
    if (!lock.tryLock()) {
      // another thread is initiating
      return;
    }

    if (MiddleLayerMetricsCollector.metricsCollector != null){
      // already initialized
      lock.unlock();
      return;
    }

    MiddleLayerMetricsCollector.metricsCollector = metricsCollector;

    // registers metrics
    // TODO: add exception handling
    // TODO: expose metrics into middleware collector.
    MiddleLayerMetricsCollector.metricsCollector.histogram().register(
        Name.DELAY,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_MIDDLELAYER,
        HelpMsg.DELAY,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    //TODO: decouple this into sent and received bucket sizes.
    MiddleLayerMetricsCollector.metricsCollector.histogram().register(
        Name.PACKET_SIZE,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_MIDDLELAYER,
        HelpMsg.PACKET_SIZE,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    MiddleLayerMetricsCollector.metricsCollector.counter().register(
        Name.MESSAGE_SENT_TOTAL,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_MIDDLELAYER,
        HelpMsg.MESSAGE_SENT_TOTAL);

    MiddleLayerMetricsCollector.metricsCollector.counter().register(
        Name.MESSAGE_RECEIVED_TOTAL,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_MIDDLELAYER,
        HelpMsg.MESSAGE_RECEIVED_TOTAL);

    lock.unlock();
  }

  /**
   * onMessageReceived is called whenever a new message received by a node.
   * It increments number of messages received by this node.
   * @param nodeId identifier of receiver.
   */
  public void onMessageReceived(UUID nodeId){
    MiddleLayerMetricsCollector.metricsCollector.counter().inc(Name.MESSAGE_RECEIVED_TOTAL, nodeId);
  }

  private static class Name {
    public static final String DELAY = "delay";
    public static final String MESSAGE_SENT_TOTAL = "message_sent_total";
    public static final String MESSAGE_RECEIVED_TOTAL = "message_received_total";
    public static final String PACKET_SIZE = "packet_size";
  }

  private static class HelpMsg {
    public static final String DELAY = "inter-node latency";
    public static final String MESSAGE_SENT_TOTAL = "total messages sent by a node";
    public static final String MESSAGE_RECEIVED_TOTAL = "total messages received by a node";
    public static final String PACKET_SIZE = "size of exchanged packet size";
  }

}
