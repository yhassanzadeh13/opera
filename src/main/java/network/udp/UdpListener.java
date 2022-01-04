package network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import network.packets.Request;

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
  // Owned resource by the `UDPUnderlay`. Used to dispatch the received
  // responses to the main thread.

  public UdpListener(DatagramSocket listenSocket, UdpUnderlay underlay) {
    this.listenSocket = listenSocket;
    this.underlay = underlay;
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
        new Thread(new UdpHandler(listenSocket, request, packet.getAddress(), packet.getPort(), underlay)).start();
        // TODO: manage the termination of the handler threads.

      } catch (SocketException e) {
        // Once the listener socket is closed by an outside thread, this point will be reached, and
        // we will stop listening.
        return;
      } catch (IOException e) {
        System.err.println("[UDPListener] Could not acquire the packet.");
        e.printStackTrace();
      }
    }
  }
}
