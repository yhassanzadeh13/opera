package scenario.finalita.events;

import network.model.Event;
import node.Identifier;


/**
 * Checks validation of transaction event.
 */
public class ConfirmTransactionEvent implements Event {
  private final Identifier transactionId;

  public ConfirmTransactionEvent(Identifier transactionId) {
    this.transactionId = transactionId;
  }
}
