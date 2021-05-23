package scenario.lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.lightchain.LightChainNode;

import java.util.UUID;

public class ConfirmBlockEvent implements Event {

  private UUID blockUUID;

  public ConfirmBlockEvent(UUID blockUUID) {
    this.blockUUID = blockUUID;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmBlockValidation(this.blockUUID);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
