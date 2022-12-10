package metrics.integration;

/**
 * Exception thrown when a container already exists, and a new one is attempted to be created.
 */
public class ContainerAlreadyExistsException extends Exception {
  public ContainerAlreadyExistsException(String message) {
    super(message);
  }
}

