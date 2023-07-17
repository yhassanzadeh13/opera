package scenario.finalita.events;

import network.model.Event;
import scenario.finalita.Block;


/**
 * Validates the given block for the node.
 * Size: Returns number of encoded bytes.
 */
public class ValidateBlockEvent implements Event {
  private final Block block;

  public ValidateBlockEvent(Block block) {
    this.block = block;
  }

  public Block getBlock() {
    return block;
  }
}
