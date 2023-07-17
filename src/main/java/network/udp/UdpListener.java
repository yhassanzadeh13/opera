package network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import modules.logger.Logger;
import modules.logger.OperaLogger;
import network.model.Request;

/**
 * Implements a routine that continuously listens a local udp port for requests and responses, and delegates
 * the handling of each received request to a `UDPHandler` thread and each received response to the main `UDPUnderlay`
 * thread.
 */
public class UdpListener implements Runnable {
  // Owned resource by the `UDPUnderlay`.
  private final DatagramSocket listenSocket;
  // Owned resource by the `UDPUnderlay`.
  private final UdpUnderlay underlay;
  private Logger logger;
  // Owned resource by the `UDPUnderlay`. Used to dispatch the received
  // responses to the main thread.

  /**
   * Constructor of the `UDPListener`.
   *
   * @param listenSocket Socket of the Listener
   * @param underlay     Underlay for the Listener
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of listen socket")
  public UdpListener(DatagramSocket listenSocket, UdpUnderlay underlay) {
    this.listenSocket = listenSocket;
    this.underlay = underlay;
    this.logger = OperaLogger.getLoggerForNodeComponent(UdpListener.class.getCanonicalName(), this.underlay.getNodeId(), "udp-listener");
  }

  @Override
  public void run() {
    while (true) {
      try {
        // Allocate the size for a packet.
        byte[] packetBytes = new byte[UdpUnderlay.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
        // Wait for a packet.
        listenSocket.receive(packet);
        // Deserialize the packet.
        Object packetObject = UdpUtils.deserialize(packet.getData(), packet.getLength());
        // handle the request in a new `UDPHandler` thread.
        Request request = (Request) packetObject;

        this.logger.debug("received a new incoming request from " + packet.getAddress().getHostAddress());
        new Thread(new UdpHandler(listenSocket, request, packet.getAddress(), packet.getPort(), underlay)).start();
        // TODO: manage the termination of the handler threads.
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
