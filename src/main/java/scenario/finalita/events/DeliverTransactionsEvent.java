package scenario.finalita.events;

import java.util.ArrayList;
import java.util.List;

import network.model.Event;
import scenario.finalita.Transaction;


/**
 * Delivers transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class DeliverTransactionsEvent implements Event {
  /**
   * List of transactions to deliver.
   */
  private final List<Transaction> transactions;

  /**
   * Constructor.
   * @param transactionList list of transactions to deliver.
   */
  public DeliverTransactionsEvent(final List<Transaction> transactionList) {
    this.transactions = transactionList;
  }

  /**
   * Returns the list of transactions (shallow copy).
   *
   * @return the list of transactions (shallow copy).
   */
  public List<Transaction> getTransactions() {
    return new ArrayList<>(transactions);
  }
}
