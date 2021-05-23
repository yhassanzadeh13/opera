package scenario.lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.lightchain.Block;
import scenario.lightchain.LightChainNode;

public class SubmitBlockEvent implements Event {

  private Block block;

  public SubmitBlockEvent(Block block) {
    this.block = block;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if(!node.isRegistry()) try {
      throw new Exception("Submit Transaction Event is submitted to a node other than registry");
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
