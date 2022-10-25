package modules.logger;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Logger {
  private final org.slf4j.Logger logger;
  private final Marker fatal = MarkerFactory.getMarker("FATAL");

  public Logger(org.slf4j.Logger logger) {
    this.logger = logger;
  }

  /**
   * Logs the given message with fatal level.
   *
   * @param message message to be logged.
   */
  public void fatal(String message) {
    this.logger.error(this.fatal, message);
    System.exit(1);
  }

  public void fatal(String message, Throwable t) {
    this.logger.error(this.fatal, message, t);
    System.exit(1);
  }

  public void fatal(String message, Object... args) {
    this.logger.error(this.fatal, message, args);
    System.exit(1);
  }

  public void fatal(String message, Throwable t, Object... args) {
    this.logger.error(this.fatal, message, t, args);
    System.exit(1);
  }

  public void warn(String message) {
    this.logger.warn(message);
  }

  public void warn(String message, Throwable t) {
    this.logger.warn(message, t);
  }

  public void warn(String message, Object... args) {
    this.logger.warn(message, args);
  }

  public void warn(String message, Throwable t, Object... args) {
    this.logger.warn(message, t, args);
  }

  public void info(String message) {
    this.logger.info(message);
  }

  public void info(String message, Object... args) {
    this.logger.info(message, args);
  }

  public void debug(String message) {
    this.logger.debug(message);
  }

  public void debug(String message, Object... args) {
    this.logger.debug(message, args);
  }

  public void trace(String message) {
    this.logger.trace(message);
  }

  public void trace(String message, Object... args) {
    this.logger.trace(message, args);
  }

  public void error(String message) {
    this.logger.error(message);
  }

  public void error(String message, Throwable t) {
    this.logger.error(message, t);
  }

  public void error(String message, Object... args) {
    this.logger.error(message, args);
  }

  public void error(String message, Throwable t, Object... args) {
    this.logger.error(message, t, args);
  }
}