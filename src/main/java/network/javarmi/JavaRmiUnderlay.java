package network.javarmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import network.Underlay;
import network.model.Message;


/**
 * Java RMI connection underlay implementation.
 */
public class JavaRmiUnderlay extends Underlay {
  JavaRmiHost host;
  private int port;

  /**
   * Connects to the Java RMI adapter of a remote server.
   *
   * @param fullAddress address of the server in the form of IP:PORT
   * @return a remote Java RMI adapter.
   */
  public JavaRmiService remote(String fullAddress) {
    if (host == null) {
      return null;
    }
    JavaRmiService remote;
    try {
      remote = (JavaRmiService) Naming.lookup("//" + fullAddress + "/node");
    } catch (MalformedURLException | RemoteException | NotBoundException e) {
      // TODO: throw illegal state exception.
      return null;
    }
    return remote;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public String getAddress() {
    return "localhost";
  }

  /**
   * Constructs a `JavaRMIHost` instance and binds it to the given port.
   *
   * @param port the port that the underlay should be bound to.
   * @return true iff the Java RMI initialization was successful.
   */
  @Override
  protected boolean initUnderlay(int port) {
    if (port == 0) {
      port++; // default port on RMI cannot be 0
    }

    try {
      host = new JavaRmiHost(this);
      // Bind this RMI adapter to the given port.
      LocateRegistry.createRegistry(port).rebind("node", host);

    } catch (ExportException e) {
      port = (port + 1) % 60000; // tries another port in this range.
      return initUnderlay(port);
    } catch (RemoteException e) {
      // TODO: throw illegal state exception.
      return false;
    }

    this.port = port;
    return true;
  }

  /**
   * Invokes the appropriate RMI method on the server with the given address.
   *
   * @param address address of the remote server.
   * @param port    port of the remote server.
   * @param request request to send.
   * @return response received from the server.
   */
  @Override
  public boolean sendMessage(String address, int port, Message request) {
    if (host == null) {
      // TODO: throw illegal state exception.
      return false;
    }
    // Connect to the remote adapter.
    JavaRmiService remote = remote(address + ":" + port);
    if (remote == null) {
      // TODO: throw illegal state exception.
      return false;
    }
    // Use the remote handler to dispatch the request.
    try {
      remote.handleRequest(request);
      return true;
    } catch (RemoteException e) {
      // TODO: throw illegal state exception.
      return false;
    }
  }

  /**
   * Terminates the Java RMI underlay service.
   */
  @Override
  public void terminate() throws IllegalStateException {
    try {
      Naming.unbind("//" + getFullAddress() + "/node");
    } catch (RemoteException | NotBoundException | MalformedURLException e) {
      throw new IllegalStateException("could not unbind the RMI service", e);
    }
  }
}
