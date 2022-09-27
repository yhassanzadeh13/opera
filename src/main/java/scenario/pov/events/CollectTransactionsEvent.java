package scenario.pov.events;

import java.util.UUID;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.pov.LightChainNode;


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
  public boolean actionPerformed(BaseNode hostNode) {
    LightChainNode node = (LightChainNode) hostNode;

    if (!node.isRegistry()) {
      try {
        throw new Exception("Collect Transaction Event is submitted to a node other than registry");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    node.collectTransactions(this.requester, this.requiredNumber);

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
