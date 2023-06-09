package scenario.finalita.events;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.packets.Event;
import node.BaseNode;
import scenario.finalita.LightChainNode;
import scenario.finalita.Transaction;


/**
 * Delivers transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class DeliverTransactionsEvent implements Event {
  private final List<Transaction> transactions;

  public List<Transaction> getTransactions() {
    return transactions;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, transactions")
  public DeliverTransactionsEvent(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
