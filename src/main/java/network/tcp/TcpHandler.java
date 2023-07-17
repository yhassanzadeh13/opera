package network.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Request;


/**
 * Represents a thread that handles an incoming tcp request and emits a response.
 */
public class TcpHandler implements Runnable {
  // tcp stream. We use this two-way stream to read the request and send back the response.
  private final Socket incomingConnection;
  // tcp underlay.
  private final TcpUnderlay underlay;
  private final Logger logger;

  /**
   * Creates a new tcp handler.
   *
   * @param incomingConnection the incoming tcp connection.
   * @param underlay           the tcp underlay.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of incomingConnection")
  public TcpHandler(Socket incomingConnection, TcpUnderlay underlay) {
    this.incomingConnection = incomingConnection;
    this.underlay = underlay;
    this.logger = OperaLogger.getLoggerForNodeComponent(TcpHandler.class.getCanonicalName(), this.underlay.getNodeId(), "tcp-handler");
  }

  // TODO send back an error response when necessary.
  @Override
  public void run() {
    ObjectInputStream requestStream;
    // Construct the streams from the connection.
    try {
      requestStream = new ObjectInputStream(incomingConnection.getInputStream());
    } catch (IOException e) {
      this.logger.fatal("could not construct the input stream from the incoming connection.", e);
      return;
    }
    // Read the request from the connection.
    Request request;
    try {
      request = (Request) requestStream.readObject();
      underlay.dispatchRequest(request);
    } catch (IOException e) {
      this.logger.fatal("could not read the request from the incoming connection.", e);
    } catch (ClassNotFoundException e) {
      // TODO: this must be an IllegalStateException.
      this.logger.fatal("could not find target class for received message", e);
    }
  }
}
