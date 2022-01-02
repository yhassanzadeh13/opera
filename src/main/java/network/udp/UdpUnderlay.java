package network.udp;

import java.io.IOException;
import java.net.*;

import network.Underlay;
import network.packets.Request;


/**
 * udp underlay implementation.
 */
public class UdpUnderlay extends Underlay {

  /**
   * The nature of udp requires us to predefine the maximum size of a packet that could be transferred. This parameter
   * defines the maximum size of a packet in bytes.
   */
  public static final int MAX_PACKET_SIZE = 512;


  // The thread that continuously listens for incoming connection in the background. As opposed to tcp, both requests
  // and responses will be received by this thread.
  private Thread listenerThread;
  // The local udp socket that can accept incoming udp connections.
  private DatagramSocket udpSocket;

  @Override
  public int getPort() {
    return udpSocket.getLocalPort();
  }

  @Override
  public String getAddress() {
    // return udpSocket.getInetAddress().getHostAddress();
    return "localhost";
  }

  /**
   * Creates a udp socket at the given port and starts listening it.
   *
   * @param port the port that the underlay should be bound to.
   * @return whether the initialization was successful.
   */
  @Override
  protected boolean initUnderlay(int port) {
    // Bind to the given port.
    try {
      udpSocket = new DatagramSocket(port);
    } catch (SocketException e) {
      System.err.println("[UDPUnderlay] Could not initialize at the given port.");
      e.printStackTrace();
      return false;
    }
    // Create the listener thread that will continuously listen to the udp packets.
    listenerThread = new Thread(new UdpListener(udpSocket, this));
    listenerThread.start();
    return true;
  }

  /**
   * Sends an udp request the given address. The size of the request in bytes cannot exceed the size
   * defined in `UDPUtils.MAX_PACKET_SIZE`.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request request to send.
   * @return the response emitted by the server.
   */
  @Override
  public boolean sendMessage(String address, int port, Request request) {
    // Convert a string address to an actual address to be used for udp.
    InetAddress destAddress;
    try {
      destAddress = Inet4Address.getByName(address);
    } catch (UnknownHostException e) {
      System.err.println("[UDPUnderlay] Could not find the host with the address " + address);
      e.printStackTrace();
      return false;
    }
    // Serialize the request.
    byte[] requestBytes = UdpUtils.serialize(request);
    if (requestBytes == null) {
      System.err.println("[UDPUnderlay] Invalid request.");
      return false;
    }
    // Then, send the request.
    DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, destAddress, port);
    try {
      udpSocket.send(requestPacket);
    } catch (IOException e) {
      log.debug("[UDPUnderlay] Could not send the request.");
      //e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Terminates the underlay by unbinding the listener from the port.
   *
   * @return whether the termination was successful.
   */
  @Override
  public boolean terminate() {
    try {
      // Unbind from the local port.
      udpSocket.close();
      // Close the listener thread.
      listenerThread.join();
    } catch (InterruptedException e) {
      System.err.println("[UDPUnderlay] Could not terminate.");
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
