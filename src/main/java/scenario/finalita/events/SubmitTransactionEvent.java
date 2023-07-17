package scenario.finalita.events;

import network.model.Event;
import scenario.finalita.Transaction;

/**
 * Adds a new transaction to a node.
 * Size: Returns number of encoded bytes.
 */
public class SubmitTransactionEvent implements Event {

    private final Transaction transaction;

    public SubmitTransactionEvent(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
