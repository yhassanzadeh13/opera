package network.local;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.model.Request;

/**
 * Sends request to a middle layer.
 */
public class LocalHandler implements Runnable {

  final Request request;

  // destination middle layer to send the request
  final network.Network destinationNetwork;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public LocalHandler(Request request, network.Network destinationNetwork) {
    this.request = request;
    this.destinationNetwork = destinationNetwork;
  }

  @Override
  public void run() {
    destinationNetwork.receive(this.request);
  }
}
