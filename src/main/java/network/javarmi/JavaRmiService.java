package network.javarmi;

import network.model.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a Java RMI Service. An RMI service only has a single function that dispatches the received request
 * to the local `RequestHandler` instance.
 */
public interface JavaRmiService extends Remote {
  void handleRequest(Message request) throws RemoteException;
}
