package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;
import scenario.PoV.RegistryNode;
import scenario.PoV.Transaction;

public class SubmitTransactionEvent implements Event {

  private Transaction transaction;

  public SubmitTransactionEvent(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    RegistryNode node = null;

    /// not sure if this is necessary right now TODO: TEST THIS
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
}
