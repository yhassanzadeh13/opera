package lightchain.events;

import Node.BaseNode;
import Underlay.packets.Event;
import lightchain.LightChainNode;

import java.util.UUID;

public class ConfirmTransactionEvent implements Event {

  private UUID transactionUUID;

  public ConfirmTransactionEvent(UUID transactionUUID) {
    this.transactionUUID = transactionUUID;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    if(!node.isRegistry()) try {
      throw new Exception("Submit Transaction Event is submitted to a node other than registry");
    } catch (Exception e) {
      e.printStackTrace();
    }

    node.confirmTransactionValidation(this.transactionUUID);

    return true;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
