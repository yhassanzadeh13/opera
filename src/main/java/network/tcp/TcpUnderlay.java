package network.tcp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

import network.Underlay;
import network.exception.OperaNetworkingException;
import network.model.Message;

/**
 * tcp underlay implementation.
 */
public class TcpUnderlay extends Underlay {
  private final HashSet<Socket> socketCache = new HashSet<>();
  private final HashMap<String, ObjectOutputStream> streamCache = new HashMap<>();
  // The thread that continuously listens for incoming connection in the background.
  private Thread listenerThread;
  // The local tcp socket that can accept incoming tcp connections.
  private ServerSocket serverSocket;

  /**
   * Sends a message to a remote node.
   *
   * @param targetAddress address of the remote node who should receive the message.
   * @param message       the message to be sent.
   * @throws OperaNetworkingException if it could not send the message.
   */
  @Override
  public void send(final InetSocketAddress targetAddress, final Message message) throws OperaNetworkingException {
    Socket remote;
    ObjectOutputStream requestStream;

    String fullAddress = targetAddress.getAddress() + ":" + targetAddress.getPort();
    requestStream = streamCache.getOrDefault(fullAddress, null);
    if (requestStream == null) {
      // Connect to the remote tcp server.
      try {
        remote = new Socket(targetAddress.getAddress(), targetAddress.getPort());
        remote.setKeepAlive(true);

        socketCache.add(remote);
        requestStream = new ObjectOutputStream(remote.getOutputStream());
        streamCache.put(fullAddress, requestStream);
      } catch (IOException e) {
        throw new OperaNetworkingException("could not connect to remote server", e);
      }
    }
    // Send the request.
    try {
      requestStream.writeObject(message);
    } catch (IOException e) {
      throw new OperaNetworkingException("could not write object on stream", e);
    }
  }

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
      // TODO: throw illegal state exception.
      return false;
    }
    // Create & start the listening thread which will continuously listen for incoming connections
    // and handle the requests as implemented in the `RequestHandler` class.
    listenerThread = new Thread(new TcpListener(serverSocket, this));
    listenerThread.start();
    return true;
  }

  /**
   * Terminates the underlay by unbinding the listener from the port.
   *
   * @throws IllegalStateException if it could not terminate the node.
   */
  @Override
  public void terminate() throws IllegalStateException {
    try {
      for (ObjectOutputStream o : this.streamCache.values()) {
        o.close();
      }
      for (Socket s : this.socketCache) {
        s.close();
      }
      serverSocket.close();
      listenerThread.join();
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("could not terminate tcp underlay", e);
    }
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
