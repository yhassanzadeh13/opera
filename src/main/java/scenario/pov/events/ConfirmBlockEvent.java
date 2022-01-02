package scenario.pov.events;

import java.util.UUID;

import network.packets.Event;
import node.BaseNode;
import scenario.pov.LightChainNode;


/**
 * It checks validation of block event in a node.
 */
public class ConfirmBlockEvent implements Event {

  private final UUID blockUuid;

  public ConfirmBlockEvent(UUID blockUuid) {
    this.blockUuid = blockUuid;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmBlockValidation(this.blockUuid);

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
