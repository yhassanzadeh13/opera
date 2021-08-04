package scenario.pov.events;

import underlay.packets.Event;
import node.BaseNode;
import scenario.pov.LightChainNode;
import scenario.pov.Transaction;

/**
 *
 */
public class SubmitTransactionEvent implements Event {

  private Transaction transaction;

  public SubmitTransactionEvent(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if (!node.isRegistry()) {
      try {
        throw new Exception("Submit Transaction Event is submitted to a node other than registry");
      } catch (Exception e) {
        e.printStackTrace();
      }
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
