package underlay.TCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Implements a routine that continuously listens a local TCP port and delegates the handling
 * of each received request to a `TCPHandler` thread.
 */
public class TCPListener implements Runnable {

    // Owned resource by the `TCPUnderlay`.
    private final ServerSocket serverSocket;
    // Owned resource by the `TCPUnderlay`.
    private final TCPUnderlay underlay;

    public TCPListener(ServerSocket serverSocket, TCPUnderlay underlay) {
        this.serverSocket = serverSocket;
        this.underlay = underlay;
    }

    @Override
    public void run() {
        while(true) {
            try {
                // Wait for an incoming connection.
                Socket incomingConnection = serverSocket.accept();
                // Handle the connection in a new thread.
                // TODO: manage the termination of the handler threads.
                new Thread(new TCPHandler(incomingConnection, underlay)).start();
            } catch(SocketException e) {
                // Once the listener socket is closed by an outside thread, this point will be reached and
                // we will stop listening.
                return;
            } catch (IOException e) {
                System.err.println("[TCPListener] Could not acquire the incoming connection.");
                e.printStackTrace();
            }
        }
    }

}
