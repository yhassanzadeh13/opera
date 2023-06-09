package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;


/**
 * Getter of the latest block event.
 * Size: Returns number of encoded bytes.
 */
public class GetLatestBlockEvent implements Event {
  private final Identifier requester;

  public GetLatestBlockEvent(Identifier requester) {
    this.requester = requester;
  }

  public Identifier getRequester() {
    return requester;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
