package scenario.finalita.events;

import network.model.Event;
import scenario.finalita.Block;


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
}
