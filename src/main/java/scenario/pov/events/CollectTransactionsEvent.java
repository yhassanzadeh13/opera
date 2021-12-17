package scenario.pov.events;

import java.util.UUID;

import node.BaseNode;
import scenario.pov.LightChainNode;
import underlay.packets.Event;


/**
 * Collects Transactions submitted to a node.
 */
public class CollectTransactionsEvent implements Event {
  private final UUID requester;
  private final Integer requiredNumber;

  public CollectTransactionsEvent(UUID requester, Integer requiredNumber) {
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
