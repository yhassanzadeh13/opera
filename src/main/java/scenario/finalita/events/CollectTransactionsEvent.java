package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
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

  @Override
  public boolean actionPerformed(BaseNode baseNode) {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  public Identifier getRequester() {
    return requester;
  }

  public Integer getRequiredNumber() {
    return requiredNumber;
  }
}
