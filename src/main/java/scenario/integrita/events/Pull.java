package scenario.integrita.events;

import network.packets.Event;

/**
 * Pull event encapsulates a client-side pull request to the server.
 */
public class Pull implements Event {

  @Override
  public int size() {
    return 0;
  }
}
