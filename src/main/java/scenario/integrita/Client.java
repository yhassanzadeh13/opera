package scenario.integrita;

import java.util.ArrayList;
import java.util.UUID;

import metrics.MetricsCollector;
import node.BaseNode;
import scenario.integrita.events.Push;
import scenario.integrita.historytree.HistoryTreeNode;
import underlay.MiddleLayer;
import underlay.packets.Event;

public class Client implements BaseNode {
    UUID id;
    MiddleLayer network;
    ArrayList<UUID> ids; // all ids inclding self

    public Client() {

    }

    public Client(UUID selfId, MiddleLayer network) {
        this.id = selfId;
        this.network = network;
    }

    @Override
    public void onCreate(ArrayList<UUID> allId) {
        this.ids = allId;
        this.network.ready();
    }

    @Override
    public void onStart() {
        for (UUID receiver : ids) {
            if (receiver.equals(this.id)) {
                continue;
            }
            // create an empty node
            Push pushMsg = new Push(new HistoryTreeNode(), "Hello");
            network.send(receiver, pushMsg);
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onNewMessage(UUID originId, Event msg) {
        System.out.println("Sender UUID: " + originId.toString() + " message " + msg.logMessage());

    }

    @Override
    public BaseNode newInstance(UUID selfId, String nameSpace, MiddleLayer network, MetricsCollector metrics) {
        Client client = new Client(selfId, network);
        return client;
    }
}
