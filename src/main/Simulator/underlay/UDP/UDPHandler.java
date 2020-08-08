package underlay.UDP;

import underlay.packets.Request;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Represents a thread that handles a UDP request and emits a response.
 */
public class UDPHandler implements Runnable {

    // The UDP socket that the response will be sent through.
    private final DatagramSocket udpSocket;
    // The received request to handle.
    private final Request request;
    // The address of the client that the request was sent from.
    private final InetAddress clientAddress;
    // The port of the client that the request was sent from.
    private final int clientPort;
    // The handler which will be handling this request.
    private final UDPUnderlay underlay;

    public UDPHandler(DatagramSocket udpSocket, Request request, InetAddress clientAddress, int clientPort,
                      UDPUnderlay underlay) {
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
//        Response response = underlay.dispatchRequest(request);
//        // Serialize the response.
//        byte[] responseBytes = UDPUtils.serialize(response);
//        if(responseBytes == null) {
//            System.err.println("[UDPHandler] Invalid response.");
//            return;
//        }
//        // Construct the response packet.
//        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
//        // Send the response packet.
//        try {
//            udpSocket.send(responsePacket);
//        } catch (IOException e) {
//            System.err.println("[UDPHandler] Could not send the response.");
//            e.printStackTrace();
//        }
    }
}
