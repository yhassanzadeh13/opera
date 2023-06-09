package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.finalita.Block;


/**
 * Appends new block to the node.
 * Size: Returns number of encoded bytes.
 */
public class SubmitBlockEvent implements Event {
  private final Block block;

  public SubmitBlockEvent(Block block) {
    this.block = block;
  }

  public Block getBlock() {
    return block;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {}

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
