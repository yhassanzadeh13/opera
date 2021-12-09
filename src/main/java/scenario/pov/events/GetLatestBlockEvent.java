package scenario.pov.events;

import java.util.UUID;

import node.BaseNode;
import scenario.pov.LightChainNode;
import underlay.packets.Event;


/**
 * Getter of the latest block event.
 * Size: Returns number of encoded bytes.
 */
public class GetLatestBlockEvent implements Event {

  private final UUID requester;

  public GetLatestBlockEvent(UUID requester) {
    this.requester = requester;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if (!node.isRegistry()) {
      try {
        throw new Exception("Submit Transaction Event is submitted to a node other than registry");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    node.getLatestBlock(this.requester);

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
