package underlay.javarmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import simulator.Simulator;
import underlay.Underlay;
import underlay.packets.Request;


/**
 * Java RMI connection underlay implementation.
 */
public class JavaRmiUnderlay extends Underlay {

  // Java RMI instance running at the host machine.
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
      System.err.println("[JavaRMIUnderlay] Host does not exist.");
      return null;
    }
    JavaRmiService remote;
    try {
      remote = (JavaRmiService) Naming.lookup("//" + fullAddress + "/node");
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not connect to the remote RMI server!");
      e.printStackTrace();
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

    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Error while initializing at port " + port);
      e.printStackTrace();
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
  public boolean sendMessage(String address, int port, Request request) {
    if (host == null) {
      System.err.println("[JavaRMIUnderlay] Host does not exist.");
      return false;
    }
    // Connect to the remote adapter.
    JavaRmiService remote = remote(address + ":" + port);
    if (remote == null) {
      System.err.println("[JavaRMIUnderlay] Could not connect to the address: " + address + ":" + port);
      return false;
    }
    // Use the remote handler to dispatch the request.
    try {
      remote.handleRequest(request);
      return true;
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not send the message.");
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Terminates the Java RMI underlay service.
   */
  @Override
  public boolean terminate(String address, int port) {
    try {
      Naming.unbind("//" + getFullAddress() + "/node");
    } catch (Exception e) {
      Simulator.getLogger().error("[JavaRMIUnderlay] Could not terminate.");
      System.err.println("[JavaRMIUnderlay] Could not terminate.");
      return false;
    }
    return true;
  }
}
