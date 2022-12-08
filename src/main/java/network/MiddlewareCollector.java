package network;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import metrics.Constants;
import metrics.Counter;
import metrics.Histogram;
import metrics.opera.OperaCounter;
import metrics.opera.OperaHistogram;
import node.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates metrics collectors for network.
 */
// TODO: rename to middleware.
public class MiddlewareCollector {
  public static final String Subsystem_Middleware = "middleware";
  private final Histogram propagationDelay;
  private final Histogram receivedMessageSize;
  private final Histogram sentMessageSize;
  private final Counter messageReceivedTotal;
  private final Counter messageSentTotal;


  /**
   * Atomically initiates metric collector for middlelayer exactly once.
   */
  public MiddlewareCollector() {
    this.propagationDelay = new OperaHistogram(Name.PROPAGATION_DELAY,
        Constants.Namespace.NETWORK, Subsystem_Middleware,
        HelpMsg.PROPAGATION_DELAY,
        Constants.Histogram.DEFAULT_HISTOGRAM,
        Constants.IDENTIFIER);
    this.receivedMessageSize = new OperaHistogram(Name.RECEIVED_MESSAGE_SIZE,
        Constants.Namespace.NETWORK, Subsystem_Middleware,
        HelpMsg.RECEIVED_MESSAGE_SIZE,
        Constants.Histogram.DEFAULT_HISTOGRAM,
        Constants.IDENTIFIER);
    this.sentMessageSize = new OperaHistogram(Name.SENT_MESSAGE_SIZE,
        Constants.Namespace.NETWORK, Subsystem_Middleware,
        HelpMsg.SENT_MESSAGE_SIZE,
        Constants.Histogram.DEFAULT_HISTOGRAM,
        Constants.IDENTIFIER);
    this.messageReceivedTotal = new OperaCounter(Name.MESSAGE_RECEIVED_TOTAL,
        Constants.Namespace.NETWORK, Subsystem_Middleware,
        HelpMsg.MESSAGE_RECEIVED_TOTAL,
        Constants.IDENTIFIER);
    this.messageSentTotal = new OperaCounter(Name.MESSAGE_SENT_TOTAL,
        Constants.Namespace.NETWORK, Subsystem_Middleware,
        HelpMsg.MESSAGE_SENT_TOTAL,
        Constants.IDENTIFIER);
  }

  /**
   * onMessageReceived is called whenever a new message received by a node.
   * It increments number of messages received by this node, as well as stops the timer for propagation delay.
   * It also records size of the message in bytes.
   *
   * @param receiverId identifier of receiver.
   * @param size       size of message in bytes.
   */
  public void onMessageReceived(Identifier receiverId, int size, @NotNull Timestamp sentTimeStamp) {
    messageReceivedTotal.increment(receiverId);
    receivedMessageSize.observe(receiverId, size);
    propagationDelay.observe(receiverId,
        Duration.between(sentTimeStamp.toLocalDateTime(), LocalDateTime.now()).toMillis());
  }

  /**
   * onMessageSent is called whenever a new message is sent by a node.
   * It increments the number of messages sent.
   * It also records size of the message in bytes.
   *
   * @param senderId identifier of sender.
   * @param size     size of message in bytes.
   */
  public void onMessageSent(Identifier senderId, int size) {
    this.messageSentTotal.increment(senderId);
    this.sentMessageSize.observe(senderId, size);
  }

  private static class Name {
    public static final String PROPAGATION_DELAY = "propagation_delay";
    public static final String MESSAGE_SENT_TOTAL = "message_sent_total";
    public static final String MESSAGE_RECEIVED_TOTAL = "message_received_total";
    public static final String RECEIVED_MESSAGE_SIZE = "received_message_size";
    public static final String SENT_MESSAGE_SIZE = "sent_message_size";
  }

  private static class HelpMsg {
    public static final String PROPAGATION_DELAY = "inter-node network.latency";
    public static final String MESSAGE_SENT_TOTAL = "total messages sent by a node";
    public static final String MESSAGE_RECEIVED_TOTAL = "total messages received by a node";
    public static final String RECEIVED_MESSAGE_SIZE = "size of received message by a node";

    public static final String SENT_MESSAGE_SIZE = "size of sent message by a node";
  }

}
