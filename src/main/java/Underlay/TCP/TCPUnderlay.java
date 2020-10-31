package Underlay.TCP;

import Simulator.Simulator;
import Underlay.Underlay;
import Underlay.packets.Request;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP underlay implementation.
 */
public class TCPUnderlay extends Underlay {

    // The thread that continuously listens for incoming connection in the background.
    private Thread listenerThread;
    // The local TCP socket that can accept incoming TCP connections.
    private ServerSocket serverSocket;

    /**
     * Creates a TCP socket at the given port and starts listening it.
     * @param port the port that the underlay should be bound to.
     * @return true iff initialization is successful.
     */

    @Override
    public boolean initUnderlay(int port) {
        try {
            // Create the TCP socket at the given port.
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("[TCPUnderlay] Could not initialize at the given port.");
            e.printStackTrace();
            return false;
        }
        // Create & start the listening thread which will continuously listen for incoming connections
        // and handle the requests as implemented in the `RequestHandler` class.
        listenerThread = new Thread(new TCPListener(serverSocket, this));
        listenerThread.start();
        return true;
    }

    /**
     *
     * @param address address of the remote server.
     * @param port port of the remote serve.r
     * @param request the request to send.
     * @return the response emitted by the remote server.
     */
    @Override
    public boolean sendMessage(String address, int port, Request request) {
        Socket remote;
        ObjectOutputStream requestStream;
        // Connect to the remote TCP server.
        try {
            remote = new Socket(address, port);
        } catch (IOException e) {
            Simulator.getLogger().error("[TCPUnderlay] Could not connect to the address: " + address + ":" + port);
            return false;
        }
        // Send the request.
        try {
            requestStream = new ObjectOutputStream(remote.getOutputStream());
            requestStream.writeObject(request);
        } catch(IOException e) {
            System.err.println("[TCPUnderlay] Could not send the request.");
            return false;
        }
        // Close the connection & streams.
        try {
            requestStream.close();
            remote.close();
        } catch (IOException e) {
            System.err.println("[TCPUnderlay] Could not close the outgoing connection.");
        }
        return true;
    }

    /**
     * Terminates the underlay by unbinding the listener from the port.
     * @return whether the termination was successful.
     */
    @Override
    public boolean terminate(String address, int port) {
        try {
            // Unbind from the local port.
            serverSocket.close();
            // Terminate the listener thread.
            listenerThread.join();
            this.log.debug("[TCPUnderlay] node " + address + ":" + port + " is begin terminated");
        } catch (Exception e) {
            System.err.println("[TCPUnderlay] Could not terminate.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
