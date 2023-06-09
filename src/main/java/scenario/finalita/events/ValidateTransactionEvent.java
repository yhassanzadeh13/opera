package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.finalita.LightChainNode;
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

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  public Transaction getTransaction() {
    return transaction;
  }
}
