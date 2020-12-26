package SkipGraph;

import Node.BaseNode;
import SkipGraph.lookup.LookupTable;
import SkipGraph.lookup.LookupTableFactory;
import SkipGraph.packets.Request;
import SkipGraph.packets.Response;
import SkipGraph.skipnode.SkipNodeIdentity;
import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the overlay.
 */
public class SkipGraphNode implements BaseNode {

    private final UUID selfId;
    private final RequestResponseLayer network;

    private List<UUID> allIDs;
    private LookupTable table;

    private SkipNodeIdentity identity;
    private boolean inserted = false;

    public SkipGraphNode(UUID selfId, RequestResponseLayer underlay) {
        this.selfId = selfId;
        this.network = underlay;
    }

    public UUID getID() {
        return selfId;
    }

    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allIDs = allID;
        int numLevels = (int) (Math.log(allID.size())/Math.log(2));
        this.table = LookupTableFactory.createDefaultLookupTable(numLevels);
        this.identity = IdentityFactory.newIdentity(numLevels);
        System.out.println("Node received identity " + identity);
        if (network != null) {
            network.getNetwork().ready();
        }
    }

    @Override
    public void onStart() {
        UUID server = allIDs.get(0);
        if(server.equals(selfId)) return;
        if (network != null) {
            network.sendRequest(server, new Request());
        }
    }

    @Override
    public void onStop() {
        // None.
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {
        msg.actionPerformed(this);
    }

    @Override
    public BaseNode newInstance(UUID selfID, MiddleLayer network) {
        // Not required, as the simulator will interact with RequestResponseLayer instances.
        return null;
    }

    public Response handleRequest(Request request) {
        return new Response(request.flowId);
    }


}
