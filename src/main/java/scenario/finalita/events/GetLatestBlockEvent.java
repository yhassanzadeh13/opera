package scenario.finalita.events;

import network.model.Event;
import node.Identifier;


/**
 * Getter of the latest block event.
 * Size: Returns number of encoded bytes.
 */
public class GetLatestBlockEvent implements Event {
    private final Identifier requester;

    public GetLatestBlockEvent(Identifier requester) {
        this.requester = requester;
    }

    public Identifier getRequester() {
        return requester;
    }
}
