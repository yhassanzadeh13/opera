package scenario.pov.events;

import node.BaseNode;
import scenario.pov.Block;
import scenario.pov.LightChainNode;
import network.packets.Event;


/**
 * Validates the given block for the node.
 * Size: Returns number of encoded bytes.
 */
public class ValidateBlockEvent implements Event {

  private Block block;

  public ValidateBlockEvent(Block block) {
    this.block = block;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.validateBlock(this.block);

    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
