package scenario.PoV.events;

import Node.BaseNode;
import Underlay.packets.Event;
import scenario.PoV.LightChainNode;

import java.util.UUID;

public class ConfirmTransactionEvent implements Event {

  private UUID transactionUUID;

  public ConfirmTransactionEvent(UUID transactionUUID) {
    this.transactionUUID = transactionUUID;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmTransactionValidation(this.transactionUUID);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
