package utils.timeouts;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Eventually is a helper class for testing. It allows to wait for a condition to be true.
 */
public class Eventually {
    /**
     * eventually waits for a condition to be true. If the condition is not true after timeoutMillis, it fails the test.
     *
     * @param timeoutMillis         the timeout in milliseconds.
     * @param pollingIntervalMillis the polling interval in milliseconds.
     * @param condition             the condition to be true.
     * @throws java.lang.Exception if the condition is not true after timeoutMillis.
     */
    public static void eventually(long timeoutMillis, long pollingIntervalMillis, Callable<Boolean> condition) throws TimeoutException, IllegalStateException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                if (condition.call()) {
                    return;
                }
                Thread.sleep(pollingIntervalMillis);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Failed to wait for condition to be true: %s", e.getMessage()));
            }
        }
        throw new TimeoutException("Timeout while waiting for condition to be true");
    }
}
