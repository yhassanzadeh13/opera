package network.local;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.MiddleLayer;
import network.packets.Request;

/**
 * Sends request to a middle layer.
 */
public class LocalHandler implements Runnable {

  final Request request;

  // destination middle layer to send the request
  final MiddleLayer destinationMiddleLayer;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public LocalHandler(Request request, MiddleLayer destinationMiddleLayer) {
    this.request = request;
    this.destinationMiddleLayer = destinationMiddleLayer;
  }

  @Override
  public void run() {
    destinationMiddleLayer.receive(this.request);
  }
}
