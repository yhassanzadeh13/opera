package scenario.lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.lightchain.LightChainNode;
import scenario.lightchain.Transaction;

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
