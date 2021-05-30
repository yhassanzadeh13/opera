package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;
import scenario.PoV.RegistryNode;

import java.util.UUID;

public class CollectTransactionsEvent implements Event {

  private UUID requester;
  private Integer requiredNumber;

  public CollectTransactionsEvent(UUID requester, Integer requiredNumber) {
    this.requester = requester;
    this.requiredNumber = requiredNumber;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    RegistryNode node = null;

    /// not sure if this is necessary right now TODO: TEST THIS
     try {
       node = (RegistryNode) hostNode;
    } catch (Exception e) {
      e.printStackTrace();
    }

    node.collectTransactions(this.requester, this.requiredNumber);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
