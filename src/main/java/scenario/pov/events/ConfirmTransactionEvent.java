package scenario.pov.events;

import java.util.UUID;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.pov.LightChainNode;


/**
 * Checks validation of transaction event.
 */
public class ConfirmTransactionEvent implements Event {

  private final Identifier transactionUuid;

  public ConfirmTransactionEvent(Identifier transactionUuid) {
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
