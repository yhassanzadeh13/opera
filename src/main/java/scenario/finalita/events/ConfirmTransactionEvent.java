package scenario.finalita.events;

import network.packets.Event;
import node.Identifier;


/**
 * Checks validation of transaction event.
 */
public class ConfirmTransactionEvent implements Event {
  private final Identifier transactionId;

  public ConfirmTransactionEvent(Identifier transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
