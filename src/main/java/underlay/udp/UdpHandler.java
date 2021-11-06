package underlay.udp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import underlay.packets.Request;


/**
 * Represents a thread that handles a udp request and emits a response.
 */
public class UdpHandler implements Runnable {

  // The udp socket that the response will be sent through.
  private final DatagramSocket udpSocket;
  // The received request to handle.
  private final Request request;
  // The address of the client that the request was sent from.
  private final InetAddress clientAddress;
  // The port of the client that the request was sent from.
  private final int clientPort;
  // The handler which will be handling this request.
  private final UdpUnderlay underlay;

  /**
   *  Constructor of the UdpHandler.
   *
   * @param udpSocket Socket of the Handler
   * @param request Request of the handler
   * @param clientAddress Address of the Client
   * @param clientPort Port of the Client
   * @param underlay underlay for the Handler
   */
  public UdpHandler(DatagramSocket udpSocket,
                    Request request,
                    InetAddress clientAddress,
                    int clientPort,
                    UdpUnderlay underlay) {
    this.udpSocket = udpSocket;
    this.request = request;
    this.clientAddress = clientAddress;
    this.clientPort = clientPort;
    this.underlay = underlay;
  }

  // TODO send back an error response when necessary.
  @Override
  public void run() {
    underlay.dispatchRequest(request);
    /*
//        Response response = underlay.dispatchRequest(request);
//        // Serialize the response.
//        byte[] responseBytes = UDPUtils.serialize(response);
//        if(responseBytes == null) {
//            System.err.println("[UDPHandler] Invalid response.");
//            return;
//        }
//        // Construct the response packet.
//        DatagramPacket responsePacket = new DatagramPacket(responseBytes,
//        responseBytes.length,
//        clientAddress,
//        clientPort);
//        // Send the response packet.
//        try {
//            udpSocket.send(responsePacket);
//        } catch (IOException e) {
//            System.err.println("[UDPHandler] Could not send the response.");
//            e.printStackTrace();
//            }
*/

  }
}
