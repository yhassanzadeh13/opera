package lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import lightchain.Block;
import lightchain.LightChainNode;
import lightchain.Transaction;

import java.util.List;

public class DeliverTransactionsEvent implements Event{

  private List<Transaction> transactions;

  public DeliverTransactionsEvent(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    // TODO: continue this part

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
