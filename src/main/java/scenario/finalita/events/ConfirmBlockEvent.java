package scenario.finalita.events;

import network.packets.Event;
import node.Identifier;


/**
 * It checks validation of block event in a node.
 */
public class ConfirmBlockEvent implements Event {
  private final Identifier blockId;

  public ConfirmBlockEvent(Identifier blockId) {
    this.blockId = blockId;
  }

  public Identifier getBlockId() {
    return blockId;
  }

  @Override
  public int size() {
    return 0;
  }
}
