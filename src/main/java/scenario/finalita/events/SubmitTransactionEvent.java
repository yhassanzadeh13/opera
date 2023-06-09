package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.finalita.LightChainNode;
import scenario.finalita.Transaction;

/**
 * Adds a new transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class SubmitTransactionEvent implements Event {

  private final Transaction transaction;

  public SubmitTransactionEvent(Transaction transaction) {
    this.transaction = transaction;
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
