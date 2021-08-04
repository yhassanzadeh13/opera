package scenario.pov.events;

import underlay.packets.Event;
import java.util.List;
import node.BaseNode;
import scenario.pov.LightChainNode;
import scenario.pov.Transaction;

/**
 *
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
