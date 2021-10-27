package scenario.pov.events;

import node.BaseNode;
import scenario.pov.RegistryNode;
import scenario.pov.Transaction;
import underlay.packets.Event;

/**
 * Adds a new transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class SubmitTransactionEvent implements Event {

  private Transaction transaction;

  public SubmitTransactionEvent(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    RegistryNode node = null;

    try {
      node = (RegistryNode) hostNode;
    } catch (Exception e) {
      e.printStackTrace();
    }

    node.addTransaction(this.transaction);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
