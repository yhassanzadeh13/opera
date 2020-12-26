package SkipGraph;

import SkipGraph.packets.Request;
import SkipGraph.packets.RequestEvent;
import SkipGraph.packets.Response;
import Underlay.MiddleLayer;

import java.util.UUID;

public class SendWorker implements Runnable {

    public final int flowId;
    public final Request request;
    public Response response = null;

    private final MiddleLayer middleLayer;
    private final UUID destination;

    public SendWorker(UUID destination, Request request, MiddleLayer middleLayer) {
        this.request = request;
        this.flowId = request.flowId;
        this.middleLayer = middleLayer;
        this.destination = destination;
    }

    public synchronized void dispatchResponse(Response response) {
        this.response = response;
        notifyAll();
    }

    @Override
    public void run() {

    }
}
