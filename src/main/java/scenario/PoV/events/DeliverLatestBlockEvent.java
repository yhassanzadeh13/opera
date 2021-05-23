package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.Block;
import scenario.PoV.LightChainNode;

public class DeliverLatestBlockEvent implements Event {

  private Block latestBlock;

  public DeliverLatestBlockEvent(Block latestBlock) {
    this.latestBlock = latestBlock;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.updateLatestBlock(this.latestBlock);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
