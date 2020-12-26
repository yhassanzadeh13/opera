package SkipGraph;

import SkipGraph.packets.Response;

import java.util.concurrent.CountDownLatch;

public class FlowMonitor {

    public Response receivedResponse = null;
    public CountDownLatch latch = new CountDownLatch(1);

    public void dispatchResponse(Response response) {
        this.receivedResponse = response;
        latch.countDown();
    }

    public Response waitForResponse() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return receivedResponse;
    }
}
