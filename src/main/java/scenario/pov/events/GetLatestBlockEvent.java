package scenario.pov.events;

import java.util.UUID;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.pov.LightChainNode;


/**
 * Getter of the latest block event.
 * Size: Returns number of encoded bytes.
 */
public class GetLatestBlockEvent implements Event {

  private final Identifier requester;

  public GetLatestBlockEvent(Identifier requester) {
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
