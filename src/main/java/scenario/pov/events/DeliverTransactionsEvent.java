package scenario.pov.events;

import java.util.List;

import network.packets.Event;
import node.BaseNode;
import scenario.pov.LightChainNode;
import scenario.pov.Transaction;


/**
 * Delivers transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class DeliverTransactionsEvent implements Event {
  private final List<Transaction> transactions;

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

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
