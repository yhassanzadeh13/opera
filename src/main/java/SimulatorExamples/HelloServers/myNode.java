package SimulatorExamples.HelloServers;

import Metrics.MetricsCollector;
import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class myNode implements BaseNode {
    private static final String MESSAGE_COUNT = "MessageCnt";
    private UUID selfID;
    private ArrayList<UUID> allID;
    private MiddleLayer network;
    private MetricsCollector mMetricsCollector;

    myNode() {}

    myNode(UUID selfID, MiddleLayer network, MetricsCollector metricsCollector) {
        this.selfID = selfID;
        this.network = network;

        //Register metrics
        this.mMetricsCollector = metricsCollector;
        mMetricsCollector.getHistogramCollector().register("packetSize", new double[]{1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 20.0});
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allID = allID;
        network.ready();
    }

    @Override
    public void onStart() {
        this.sendNewMessage("Hello");
    }

    public void sendNewMessage(String msg) {
        if (allID.isEmpty())
            return;
        Random rand = new Random();
        int ind = rand.nextInt(allID.size());
        SendHello helloMessage = new SendHello(msg, selfID, allID.get(ind));
        network.send(allID.get(ind), helloMessage);
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {
        try {
            Random rand = new Random();
            Thread.sleep(rand.nextInt(1000));
        }
        catch (InterruptedException e) {}
        msg.actionPerformed(this);
    }

    @Override
    public BaseNode newInstance(UUID ID, MiddleLayer network, MetricsCollector metricsCollector) {
        return new myNode(ID, network, metricsCollector);
    }
}
