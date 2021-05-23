package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.Block;
import scenario.PoV.LightChainNode;

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
  public String logMessage() {
    return null;
  }
}
