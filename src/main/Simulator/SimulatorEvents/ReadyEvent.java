package SimulatorEvents;

import Node.BaseNode;
import underlay.packets.Event;

import java.util.UUID;

public class ReadyEvent implements Event {

    private UUID nodeID;
    private String fullAddress;

    /**
     * create new ready event. Used by the node to declare itself as ready
     * @param nodeID UUID of the node
     * @param fullAddress full address of the node
     */
    ReadyEvent(UUID nodeID, String fullAddress)
    {
        this.nodeID = nodeID;
        this.fullAddress = fullAddress;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        return true;
    }

    @Override
    public String logMessage() {
        return "[" + this.fullAddress + "] node is ready";
    }
}
