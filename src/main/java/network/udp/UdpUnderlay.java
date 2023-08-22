package network.udp;

import java.io.IOException;
import java.net.*;

import network.Underlay;
import network.exception.OperaNetworkingException;
import network.model.Message;


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
      // TODO: throw exception.
      return false;
    }
    // Create the listener thread that will continuously listen to the udp packets.
    listenerThread = new Thread(new UdpListener(udpSocket, this));
    listenerThread.start();
    return true;
  }

  /**
   * Sends a message to a remote node. The size of request cannot exceed the maximum packet size MAX_PACKET_SIZE.
   *
   * @param targetAddress address of the remote node who should receive the message.
   * @param message       the message to be sent.
   * @throws network.exception.OperaNetworkingException if it could not send the message.
   */
  @Override
  public void send(final InetSocketAddress targetAddress, final Message message) throws OperaNetworkingException {
    // Convert a string address to an actual address to be used for udp.
    InetAddress destAddress;
    try {
      destAddress = Inet4Address.getByName(targetAddress.getAddress().getHostAddress());
    } catch (UnknownHostException e) {
      throw new OperaNetworkingException(String.format("could not convert address into inet %s", targetAddress.toString()), e);
    }
    // Serialize the request.
    byte[] requestBytes = UdpUtils.serialize(message);
    if (requestBytes == null) {
      throw new OperaNetworkingException(String.format("could not serialize request %s", message.toString()));
    }
    // Then, send the request.
    DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, destAddress, targetAddress.getPort());
    try {
      udpSocket.send(packet);
    } catch (IOException e) {
      throw new OperaNetworkingException(String.format("could not send request %s", message.toString()), e);
    }
  }

  /**
   * Terminates the underlay by unbinding the listener from the port.
   *
   * @throws IllegalStateException if it could not terminate the node.
   */
  @Override
  public void terminate() throws IllegalStateException {
    try {
      udpSocket.close();
      listenerThread.join();
    } catch (InterruptedException e) {
      throw new IllegalStateException("could not terminate udp underlay.", e);
    }
  }
}
