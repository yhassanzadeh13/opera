package utils;

import network.packets.Event;

/**
 * A basic Event to use to check whether Utils coded correctly.
 */
public class FixtureEvent implements Event {

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
