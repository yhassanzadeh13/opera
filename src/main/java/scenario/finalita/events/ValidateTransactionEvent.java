package scenario.finalita.events;

import network.packets.Event;
import scenario.finalita.Transaction;


/**
 * Validates the given transaction for the node.
 * Size: Returns number of encoded bytes.
 */
public class ValidateTransactionEvent implements Event {

  private final Transaction transaction;

  public ValidateTransactionEvent(Transaction tx) {
    this.transaction = tx;
  }

  public Transaction getTransaction() {
    return transaction;
  }
}
