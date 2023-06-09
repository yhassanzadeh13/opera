package scenario.finalita.events;

import network.packets.Event;
import node.BaseNode;
import node.Identifier;
import scenario.finalita.LightChainNode;


/**
 * Checks validation of transaction event.
 */
public class ConfirmTransactionEvent implements Event {
  private final Identifier transactionId;

  public ConfirmTransactionEvent(Identifier transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {

    LightChainNode node = (LightChainNode) hostNode;

    node.confirmTransactionValidation(this.transactionId);

    return true;
  }

  @Override
  public int size() {
    // TODO: return number of encoded bytes
    return 1;
  }
}
