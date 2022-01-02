package scenario.pov.events;

import network.packets.Event;
import node.BaseNode;
import scenario.pov.Block;
import scenario.pov.LightChainNode;


/**
 * updates nodes block to the latestBlock.
 */
public class DeliverLatestBlockEvent implements Event {

  private final Block latestBlock;

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

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
