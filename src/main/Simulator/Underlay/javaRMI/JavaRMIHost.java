package Underlay.javaRMI;

import Underlay.packets.Request;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Represents the Java RMI Service implementation.
 */
public class JavaRMIHost extends UnicastRemoteObject implements JavaRMIService {

    private final JavaRMIUnderlay underlay;

    public JavaRMIHost(JavaRMIUnderlay underlay) throws RemoteException {
        this.underlay = underlay;
    }

    @Override
    public void handleRequest(Request request) {
        underlay.dispatchRequest(request);
    }
}
