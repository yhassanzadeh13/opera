package scenario.pov.events;

import underlay.packets.Event;
import node.BaseNode;
import scenario.pov.LightChainNode;
import scenario.pov.Transaction;

/**
 *
 */
public class ValidateTransactionEvent implements Event {

  private Transaction transaction;

  public ValidateTransactionEvent(Transaction tx) {
    this.transaction = tx;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;
    node.validateTransaction(this.transaction);

    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
