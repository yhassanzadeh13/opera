package scenario.pov.events;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.pov.LightChainNode;


/**
 * It checks validation of block event in a node.
 */
public class ConfirmBlockEvent implements Event {

  private final Identifier blockId;

  public ConfirmBlockEvent(Identifier blockId) {
    this.blockId = blockId;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmBlockValidation(this.blockId);

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
