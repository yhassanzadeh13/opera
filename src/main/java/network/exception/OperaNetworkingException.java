package network.exception;

/**
 * OperaNetworkingException is a generic exception for the Opera networking.
 */
public class OperaNetworkingException extends RuntimeException {
  public OperaNetworkingException(String message) {
    super(message);
  }

  public OperaNetworkingException(String message, Throwable cause) {
    super(message, cause);
  }
}
