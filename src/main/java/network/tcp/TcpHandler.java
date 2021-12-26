package network.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import network.packets.Request;
import simulator.Simulator;


/**
 * Represents a thread that handles an incoming tcp request and emits a response.
 */
public class TcpHandler implements Runnable {

  // tcp stream. We use this two-way stream to read the request and send back the response.
  private final Socket incomingConnection;
  // tcp underlay.
  private final TcpUnderlay underlay;

  public TcpHandler(Socket incomingConnection, TcpUnderlay underlay) {
    this.incomingConnection = incomingConnection;
    this.underlay = underlay;
  }

  // TODO send back an error response when necessary.
  @Override
  public void run() {
    ObjectInputStream requestStream;
    // Construct the streams from the connection.
    try {
      requestStream = new ObjectInputStream(incomingConnection.getInputStream());
    } catch (IOException e) {
      Simulator.getLogger().error("[TCPHandler] Could not acquire the streams from the connection.");
      return;
    }
    // Read the request from the connection.
    Request request;
    try {
      request = (Request) requestStream.readObject();
      underlay.dispatchRequest(request);
    } catch (IOException | ClassNotFoundException e) {
      Simulator.getLogger().error("[TCPHandler] Could not read the request.");
      Simulator.getLogger().error(e.getMessage());
      e.printStackTrace();
      return;
    }
  }
}
