package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.Block;
import scenario.PoV.LightChainNode;
import scenario.PoV.RegistryNode;

public class SubmitBlockEvent implements Event {

  private Block block;

  public SubmitBlockEvent(Block block) {
    this.block = block;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    RegistryNode node = null;

    /// not sure if this is necessary right now TODO: TEST THIS
    try {
      node = (RegistryNode) hostNode;
    } catch (Exception e) {
      e.printStackTrace();
    }

    node.appendBlock(this.block);
    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
