package scenario.pov.events;

import underlay.packets.Event;
import java.util.UUID;
import node.BaseNode;
import scenario.pov.LightChainNode;

/**
 *
 */
public class ConfirmTransactionEvent implements Event {

  private UUID transactionUuid;

  public ConfirmTransactionEvent(UUID transactionUuid) {
    this.transactionUuid = transactionUuid;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmTransactionValidation(this.transactionUuid);

    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }

  @Override
  public String logMessage() {
    return null;
  }
}
