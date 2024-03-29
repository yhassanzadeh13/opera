package network.javarmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.model.Message;


/**
 * Represents the Java RMI Service implementation.
 */
public class JavaRmiHost extends UnicastRemoteObject implements network.javarmi.JavaRmiService {
  @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "it is meant to expose internal state of MiddleLayer")
  private final JavaRmiUnderlay underlay;

  public JavaRmiHost(JavaRmiUnderlay underlay) throws RemoteException {
    this.underlay = underlay;
  }

  @Override
  public void handleRequest(Message request) {
    underlay.dispatchRequest(request);
  }
}
