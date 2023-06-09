package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.finalita.Block;
import scenario.finalita.LightChainNode;


/**
 * updates nodes block to the latestBlock.
 */
public class DeliverLatestBlockEvent implements Event {
  private final Block latestBlock;

  public DeliverLatestBlockEvent(Block latestBlock) {
    this.latestBlock = latestBlock;
  }

  public Block getLatestBlock() {
    return latestBlock;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
