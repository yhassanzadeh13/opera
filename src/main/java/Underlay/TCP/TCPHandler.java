package Underlay.TCP;

import Simulator.Simulator;
import Underlay.packets.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a thread that handles an incoming TCP request and emits a response.
 */
public class TCPHandler implements Runnable {

    // TCP stream. We use this two-way stream to read the request and send back the response.
    private final Socket incomingConnection;
    // TCP underlay.
    private final TCPUnderlay underlay;

    public TCPHandler(Socket incomingConnection, TCPUnderlay underlay) {
        this.incomingConnection = incomingConnection;
        this.underlay = underlay;
    }

    // TODO send back an error response when necessary.
    @Override
    public void run() {
        ObjectInputStream requestStream;
        ObjectOutputStream responseStream;
        // Construct the streams from the connection.
        try {
            requestStream = new ObjectInputStream(incomingConnection.getInputStream());
            responseStream = new ObjectOutputStream(incomingConnection.getOutputStream());
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
            return;
        }
        // Close the connection & streams.
        try {
            requestStream.close();
            responseStream.close();
            incomingConnection.close();
        } catch (IOException e) {
            System.err.println("[TCPHandler] Could not close the incoming connection.");
            e.printStackTrace();
        }
    }
}
