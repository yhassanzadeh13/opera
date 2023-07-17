package network.tcp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Implements a routine that continuously listens a local tcp port and delegates the handling
 * of each received request to a `TCPHandler` thread.
 */
public class TcpListener implements Runnable {
  // Owned resource by the `TCPUnderlay`.
  private final ServerSocket serverSocket;
  // Owned resource by the `TCPUnderlay`.
  private final TcpUnderlay underlay;
  private final Logger logger;

  /**
   * Constructor of the `TCPListener`.
   *
   * @param serverSocket Socket of the Listener.
   * @param underlay     Underlay for the Listener.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of serverSocket")
  public TcpListener(ServerSocket serverSocket, TcpUnderlay underlay) {
    this.serverSocket = serverSocket;
    this.underlay = underlay;
    this.logger = OperaLogger.getLoggerForNodeComponent(TcpListener.class.getCanonicalName(), this.underlay.getNodeId(), "tcp-listener");
  }

  @Override
  public void run() {
    while (true) {
      try {
        // Wait for an incoming connection.
        Socket incomingConnection = serverSocket.accept();
        this.logger.debug("received a new incoming connection from " + incomingConnection.getInetAddress().getHostAddress());

        // Handle the connection in a new thread.
        // TODO: manage the termination of the handler threads.
        new Thread(new TcpHandler(incomingConnection, underlay)).start();
      } catch (SocketException e) {
        // Once the listener socket is closed by an outside thread, this point will be reached, and
        // we will stop listening.
        this.logger.debug("listener socket closed.");
        return;
      } catch (IOException e) {
        this.logger.fatal("could not accept incoming connection.", e);
      }
    }
  }

}
