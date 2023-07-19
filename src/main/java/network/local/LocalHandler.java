package network.local;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.model.Message;

/**
 * Sends request to a middle layer.
 */
public class LocalHandler implements Runnable {

  final Message request;

  // destination middle layer to send the request
  final network.Network destinationNetwork;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of MiddleLayer")
  public LocalHandler(Message request, network.Network destinationNetwork) {
    this.request = request;
    this.destinationNetwork = destinationNetwork;
  }

  @Override
  public void run() {
    destinationNetwork.receive(this.request);
  }
}
