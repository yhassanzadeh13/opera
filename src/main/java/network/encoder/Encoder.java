package network.encoder;

import network.packets.Event;

public interface Encoder {
  byte[] encode(Event event);

  Event decode(byte[] bytes) throws IllegalStateException;
}
