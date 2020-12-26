package SkipGraph;

import SkipGraph.lookup.LookupTable;
import SkipGraph.lookup.LookupTableFactory;
import SkipGraph.packets.Request;
import SkipGraph.packets.Response;
import SkipGraph.skipnode.SkipNodeIdentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkipGraphNode {

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

    public void onStart() {
        UUID server = allIDs.get(0);
        if(server.equals(selfId)) return;
        if (network != null) {
            network.sendRequest(server, new Request());
        }
    }

    public void onStop() {
        // None.
    }

    public Response handleRequest(Request request) {
        return new Response(request.flowId);
    }


}
