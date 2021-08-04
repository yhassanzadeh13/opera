//package underlay.udp;
//
///**
// * Used to synchronize the sender thread with the receiver thread at the host. The sender thread (i.e. UDPUnderlay) will
// * be waiting for a response while the receiver thread (i.e. UDPListener) receives the response. Using this object, we
// * can transfer the response from the receiver thread to the sender thread.
// */
//public class UDPResponseLock {
//
//    private Response response;
//
//    /**
//     * Called by the sender thread to wait for the listener thread to receive a response. Return the response once
//     * the listener thread invokes the `dispatch` method on the lock.
//     * @return the response received from the listener thread.
//     */
//    public synchronized Response waitForResponse() {
//        try {
//            wait();
//        } catch (InterruptedException e) {
//            System.err.println("[UDPResponseLock] Error while waiting for a response.");
//            e.printStackTrace();
//            return null;
//        }
//        Response r = response;
//        response = null;
//        return r;
//    }
//
//    /**
//     * Called by the listener thread once it has received a response. Results in the waiting thread (i.e. sender thread)
//     * to stop waiting and receive the dispatched response.
//     * @param response the response to dispatch to the sender thread.
//     */
//    public synchronized void dispatch(Response response) {
//        this.response = response;
//        notify();
//    }
//}
