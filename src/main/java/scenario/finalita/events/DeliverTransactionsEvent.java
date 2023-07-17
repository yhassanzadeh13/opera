package scenario.finalita.events;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.model.Event;
import scenario.finalita.Transaction;

import java.util.List;


/**
 * Delivers transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class DeliverTransactionsEvent implements Event {
  private final List<Transaction> transactions;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, transactions")
  public DeliverTransactionsEvent(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }
}
