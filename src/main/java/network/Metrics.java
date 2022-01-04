package network;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import metrics.Constants;
import metrics.MetricsCollector;
import utils.SimulatorUtils;

/**
 * Encapsulates metrics collectors for the network layer.
 */
public class Metrics {
  public static final String SUBSYSTEM_NETWORK = "network";
  private static final ReentrantLock lock = new ReentrantLock();
  private static MetricsCollector metricsCollector;

  /**
   * Atomically initiates metric collector for the networking layer exactly once.
   *
   * @param metricsCollector root metric collector.
   */
  public Metrics(MetricsCollector metricsCollector) {
    if (!lock.tryLock()) {
      // another thread is initiating
      return;
    }

    if (Metrics.metricsCollector != null) {
      // already initialized
      lock.unlock();
      return;
    }

    Metrics.metricsCollector = metricsCollector;

    // registers metrics
    // TODO: add exception handling
    // TODO: expose metrics into network collector.
    Metrics.metricsCollector.histogram().register(
        Name.PROPAGATION_DELAY,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_NETWORK,
        HelpMsg.PROPAGATION_DELAY,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    //TODO: decouple this into sent and received bucket sizes.
    Metrics.metricsCollector.histogram().register(
        Name.PACKET_SIZE,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_NETWORK,
        HelpMsg.PACKET_SIZE,
        Constants.Histogram.DEFAULT_HISTOGRAM);

    Metrics.metricsCollector.counter().register(
        Name.MESSAGE_SENT_TOTAL,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_NETWORK,
        HelpMsg.MESSAGE_SENT_TOTAL);

    Metrics.metricsCollector.counter().register(
        Name.MESSAGE_RECEIVED_TOTAL,
        Constants.Namespace.NETWORK,
        SUBSYSTEM_NETWORK,
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
    Metrics.metricsCollector.counter().inc(Name.MESSAGE_RECEIVED_TOTAL, receiverId);
    LocalDateTime sentTime = sentTimeStamp.toLocalDateTime();
    LocalDateTime receivedTime = LocalDateTime.now();
    Duration propagationDelay = Duration.between(sentTime, receivedTime);

    Metrics.metricsCollector.histogram().observe(Name.PROPAGATION_DELAY,
        receiverId,
        propagationDelay.toMillis());
    Metrics.metricsCollector.histogram().observe(Name.PACKET_SIZE, senderId, size);
  }

  /**
   * onMessageSent is called whenever a new message is sent by a node.
   * It increments the number of messages sent, as well as starts a timer for propagation delay.
   *
   * @param senderId   identifier of sender.
   * @param receiverId identifier of receiver.
   */
  public void onMessageSent(UUID senderId, UUID receiverId) {
    Metrics.metricsCollector.counter().inc(Name.MESSAGE_SENT_TOTAL, senderId);
  }

  /**
   * Uniquely hashes the pair of (senderId, receiverId) into a hash value that is used to keep track of
   * propagation delay.
   *
   * @param senderId   identifier of sender.
   * @param receiverId identifier of receiver.
   * @return hash value of the (senderId, receiverId)
   */
  private String delayBucketHash(UUID senderId, UUID receiverId) {
    return SimulatorUtils.hashPairOfNodes(senderId, receiverId);
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
