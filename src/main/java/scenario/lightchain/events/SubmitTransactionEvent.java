package scenario.lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.lightchain.LightChainNode;
import scenario.lightchain.Transaction;

public class SubmitTransactionEvent implements Event {

  private Transaction transaction;

  public SubmitTransactionEvent(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if(!node.isRegistry()) try {
      throw new Exception("Submit Transaction Event is submitted to a node other than registry");
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
