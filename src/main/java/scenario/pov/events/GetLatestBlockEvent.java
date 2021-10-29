package scenario.pov.events;

import java.util.UUID;
import node.BaseNode;
import scenario.pov.RegistryNode;
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

    RegistryNode node = null;

    try {
      node = (RegistryNode) hostNode;
    } catch (Exception e) {
      e.printStackTrace();
    }

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
