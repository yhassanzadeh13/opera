package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;
import scenario.PoV.Transaction;

import java.util.List;

public class DeliverTransactionsEvent implements Event{

  private List<Transaction> transactions;

  public DeliverTransactionsEvent(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.deliverTransactions(this.transactions);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
