package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;
import scenario.PoV.RegistryNode;

import java.util.UUID;

public class GetLatestBlockEvent implements Event {

  private UUID requester;

  public GetLatestBlockEvent(UUID requester) {
    this.requester = requester;
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

    node.getLatestBlock(this.requester);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
