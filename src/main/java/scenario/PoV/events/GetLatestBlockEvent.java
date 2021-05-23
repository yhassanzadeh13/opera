package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;

import java.util.UUID;

public class GetLatestBlockEvent implements Event {

  private UUID requester;

  public GetLatestBlockEvent(UUID requester) {
    this.requester = requester;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if(!node.isRegistry()) try {
      throw new Exception("Submit Transaction Event is submitted to a node other than registry");
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
