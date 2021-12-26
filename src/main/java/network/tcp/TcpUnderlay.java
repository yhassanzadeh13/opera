package network.tcp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

import simulator.Simulator;
import network.Underlay;
import network.packets.Request;


/**
 * tcp underlay implementation.
 */
public class TcpUnderlay extends Underlay {

  // The thread that continuously listens for incoming connection in the background.
  private Thread listenerThread;
  // The local tcp socket that can accept incoming tcp connections.
  private ServerSocket serverSocket;
  private final HashSet<Socket> socketCache = new HashSet<>();
  private final HashMap<String, ObjectOutputStream> streamCache = new HashMap<>();

  /**
   * Creates a tcp socket at the given port and starts listening it.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff initialization is successful.
   */

  @Override
  public boolean initUnderlay(int port) {
    try {
      // Create the tcp socket at the given port.
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      System.err.println("[TCPUnderlay] Could not initialize at the given port.");
      e.printStackTrace();
      return false;
    }
    // Create & start the listening thread which will continuously listen for incoming connections
    // and handle the requests as implemented in the `RequestHandler` class.
    listenerThread = new Thread(new TcpListener(serverSocket, this));
    listenerThread.start();
    return true;
  }

  /**
   * Sends request using the port and address of the remote server and gets the response.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server
   * @param request the request to send.
   * @return the response emitted by the remote server.
   */
  @Override
  public boolean sendMessage(String address, int port, Request request) {
    Socket remote;
    ObjectOutputStream requestStream;

    String fullAddress = address + ":" + port;
    requestStream = streamCache.getOrDefault(fullAddress, null);

    if (requestStream == null) {
      // Connect to the remote tcp server.
      try {
        remote = new Socket(address, port);
        remote.setKeepAlive(true);

        socketCache.add(remote);
        requestStream = new ObjectOutputStream(remote.getOutputStream());
        streamCache.put(fullAddress, requestStream);
      } catch (IOException e) {
        Simulator.getLogger().error(e.getMessage());
        return false;
      }
    }
    // Send the request.
    try {
      requestStream.writeObject(request);
    } catch (IOException e) {
      System.err.println("[TCPUnderlay] Could not send the request.");
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
  public boolean terminate(String address, int port) {
    try {
      // Terminating cached sockets and streams
      for (ObjectOutputStream o : this.streamCache.values()) {
        o.close();
      }
      for (Socket s : this.socketCache) {
        s.close();
      }
      // Unbind from the local port.
      serverSocket.close();
      // Terminate the listener thread.
      listenerThread.join();
      log.debug("[TCPUnderlay] node " + address + ":" + port + " is begin terminated");
    } catch (Exception e) {
      System.err.println("[TCPUnderlay] Could not terminate.");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public int getPort() {
    return serverSocket.getLocalPort();
  }

  @Override
  public String getAddress() {
    // return serverSocket.getInetAddress().getHostAddress();
    return "localhost";
  }
}
