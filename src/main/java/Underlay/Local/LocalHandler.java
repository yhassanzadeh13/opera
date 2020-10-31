package Underlay.Local;

import Underlay.MiddleLayer;
import Underlay.packets.Request;

public class LocalHandler implements Runnable {


    final Request request;

    // destination middle layer to send the request
    final MiddleLayer destinationMiddleLayer;

    public LocalHandler(Request request, MiddleLayer destinationMiddleLayer) {
        this.request = request;
        this.destinationMiddleLayer = destinationMiddleLayer;
    }

    @Override
    public void run() {
        destinationMiddleLayer.receive(this.request);
    }
}
