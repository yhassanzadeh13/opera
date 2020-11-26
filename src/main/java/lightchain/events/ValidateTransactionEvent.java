package lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import lightchain.LightChainNode;
import lightchain.Transaction;

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
  public String logMessage() {
    return null;
  }
}
