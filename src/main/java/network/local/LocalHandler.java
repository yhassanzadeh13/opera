package network.local;

import network.Network;
import network.packets.Request;

/**
 * Sends request to a middle layer.
 */
public class LocalHandler implements Runnable {


  final Request request;

  // destination middle layer to send the request
  final Network destinationNetwork;

  public LocalHandler(Request request, Network destinationNetwork) {
    this.request = request;
    this.destinationNetwork = destinationNetwork;
  }

  @Override
  public void run() {
    destinationNetwork.receive(this.request);
  }
}
