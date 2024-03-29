package scenario.finalita.events;

import network.model.Event;
import node.Identifier;


/**
 * Collects Transactions submitted to a node.
 */
public class CollectTransactionsEvent implements Event {
  private final Identifier requester;
  private final Integer requiredNumber;

  public CollectTransactionsEvent(Identifier requester, Integer requiredNumber) {
    this.requester = requester;
    this.requiredNumber = requiredNumber;
  }

  public Identifier getRequester() {
    return requester;
  }

  public Integer getRequiredNumber() {
    return requiredNumber;
  }
}
