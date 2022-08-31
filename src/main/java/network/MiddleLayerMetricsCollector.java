package network;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import metrics.Constants;
import metrics.MetricsCollector;

/**
 * Encapsulates metrics collectors for middlelayer of network.
 */
public class MiddleLayerMetricsCollector {
  public static final String SUBSYSTEM_MIDDLELAYER = "middlelayer";
  private static final ReentrantLock lock = new ReentrantLock();
  private static MetricsCollector metricsCollector;

  /**
   * Atomically initiates metric collector for middlelayer exactly once.
   *
   * @param metricsCollector root metric collector.
   */
  public MiddleLayerMetricsCollector(MetricsCollector metricsCollector) {
    if (!lock.tryLock()) {
      // another thread is initiating
      return;
    }

    if (MiddleLayerMetricsCollector.metricsCollector != null) {
      // already initialized
      lock.unlock();
      return;
    }

    MiddleLayerMetricsCollector.metricsCollector = metricsCollector;

    // registers metrics
    // TODO: add exception handling
    // TODO: expose metrics into middleware collector.
    MiddleLayerMetricsCollector.metricsCollector.histogram().register(
        Name.PROPAGATION_DELAY,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_MIDDLELAYER,
        HelpMsg.PROPAGATION_DELAY,
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
   * It increments number of messages received by this node, as well as stops the timer for propagation delay.
   * It also records size of the message in bytes.
   *
   * @param receiverId identifier of receiver.
   * @param senderId   identifier of sender.
   * @param size       size of message in bytes.
   */
  public void onMessageReceived(UUID receiverId, UUID senderId, int size, Timestamp sentTimeStamp) {
    MiddleLayerMetricsCollector.metricsCollector.counter().inc(Name.MESSAGE_RECEIVED_TOTAL, receiverId);
    LocalDateTime sentTime = sentTimeStamp.toLocalDateTime();
    LocalDateTime receivedTime = LocalDateTime.now();
    Duration propagationDelay = Duration.between(sentTime, receivedTime);

    MiddleLayerMetricsCollector.metricsCollector.histogram().observe(Name.PROPAGATION_DELAY,
        receiverId,
        propagationDelay.toMillis());
    MiddleLayerMetricsCollector.metricsCollector.histogram().observe(Name.PACKET_SIZE, senderId, size);
  }

  /**
   * onMessageSent is called whenever a new message is sent by a node.
   * It increments the number of messages sent, as well as starts a timer for propagation delay.
   *
   * @param senderId   identifier of sender.
   * @param receiverId identifier of receiver.
   */
  public void onMessageSent(UUID senderId, UUID receiverId) {
    MiddleLayerMetricsCollector.metricsCollector.counter().inc(Name.MESSAGE_SENT_TOTAL, senderId);
  }

  private static class Name {
    public static final String PROPAGATION_DELAY = "propagation_delay";
    public static final String MESSAGE_SENT_TOTAL = "message_sent_total";
    public static final String MESSAGE_RECEIVED_TOTAL = "message_received_total";
    public static final String PACKET_SIZE = "packet_size";
  }

  private static class HelpMsg {
    public static final String PROPAGATION_DELAY = "inter-node network.latency";
    public static final String MESSAGE_SENT_TOTAL = "total messages sent by a node";
    public static final String MESSAGE_RECEIVED_TOTAL = "total messages received by a node";
    public static final String PACKET_SIZE = "size of exchanged packet size";
  }

}
