package scenario.pov.events;

import node.BaseNode;
import scenario.pov.Block;

import scenario.pov.RegistryNode;
import underlay.packets.Event;


/**
 * Appends new block to the node.
 * Size: Returns number of encoded bytes.
 */
public class SubmitBlockEvent implements Event {

  private Block block;

  public SubmitBlockEvent(Block block) {
    this.block = block;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    RegistryNode node = null;

    try {
      node = (RegistryNode) hostNode;
    } catch (Exception e) {
      e.printStackTrace();
    }

    node.appendBlock(this.block);
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
