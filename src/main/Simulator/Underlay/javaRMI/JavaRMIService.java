package Underlay.javaRMI;

import Underlay.packets.Request;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a Java RMI Service. A RMI service only has a single function that dispatches the received request
 * to the local `RequestHandler` instance.
 */
public interface JavaRMIService extends Remote {
    void handleRequest(Request request) throws RemoteException;
}
