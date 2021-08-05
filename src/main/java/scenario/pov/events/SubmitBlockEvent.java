package scenario.pov.events;

import node.BaseNode;
import scenario.pov.Block;
import scenario.pov.LightChainNode;
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

    LightChainNode node = (LightChainNode) hostNode;

    if (!node.isRegistry()) {
      try {
        throw new Exception("Submit Transaction Event is submitted to a node other than registry");
      } catch (Exception e) {
        e.printStackTrace();
      }
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
