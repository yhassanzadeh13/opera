package modules.logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Encapsulates logger for opera project. This class empowers the fatal level log on top of the slf4j logger.
 */
public class Logger {
  private final org.slf4j.Logger logger;
  private final Marker fatal = MarkerFactory.getMarker("FATAL");
  private String prefix;

  /**
   * Constructor for Logger.
   *
   * @param logger logger for the class.
   */
  public Logger(org.slf4j.Logger logger) {
    this.logger = logger;
  }

  /**
   * Adds a prefix to the log message.
   *
   * @param prefix prefix to be added.
   */
  public void addPrefix(String prefix) {
    if (this.prefix == null) {
      this.prefix = prefix + " ";
    } else {
      this.prefix += prefix + " ";
    }
  }

  /**
   * Logs the given message with fatal level.
   *
   * @param message message to be logged.
   */
  @SuppressFBWarnings(value = "DM_EXIT", justification = "expected to exit the program")
  public void fatal(String message) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message);
    } else {
      this.logger.error(this.fatal, message);
    }
    System.exit(1);
  }

  /**
   * Logs the given message with fatal level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   */
  @SuppressFBWarnings(value = "DM_EXIT", justification = "expected to exit the program")
  public void fatal(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, t);
    } else {
      this.logger.error(this.fatal, message, t);
    }

    System.exit(1);
  }

  /**
   * Logs the given message with fatal level.
   *
   * @param message message to be logged.
   * @param args    arguments to be logged.
   */
  @SuppressFBWarnings(value = "DM_EXIT", justification = "expected to exit the program")
  public void fatal(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, args);
    } else {
      this.logger.error(this.fatal, message, args);
    }

    System.exit(1);
  }

  /**
   * Logs the given message with fatal level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   * @param args    arguments to be logged.
   */
  @SuppressFBWarnings(value = "DM_EXIT", justification = "expected to exit the program")
  public void fatal(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, t, args);
    } else {
      this.logger.error(this.fatal, message, t, args);
    }

    System.exit(1);
  }

  /**
   * Logs the given message with warn level.
   *
   * @param message message to be logged.
   */
  public void warn(String message) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message);
    } else {
      this.logger.warn(message);
    }
  }

  /**
   * Logs the given message with warn level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   */
  public void warn(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, t);
    } else {
      this.logger.warn(message, t);
    }
  }

  /**
   * Logs the given message with warn level.
   *
   * @param message message to be logged.
   * @param args    arguments to be logged.
   */
  public void warn(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, args);
    } else {
      this.logger.warn(message, args);
    }
  }

  /**
   * Logs the given message with warn level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   * @param args    arguments to be logged.
   */
  public void warn(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, t, args);
    } else {
      this.logger.warn(message, t, args);
    }
  }

  /**
   * Logs the given message with info level.
   *
   * @param message message to be logged.
   */
  public void info(String message) {
    if (this.prefix != null) {
      this.logger.info(this.prefix + message);
    } else {
      this.logger.info(message);
    }
  }

  /**
   * Logs the given message with info level.
   *
   * @param message message to be logged.
   * @param args    arguments to be logged.
   */
  public void info(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.info(this.prefix + message, args);
    } else {
      this.logger.info(message, args);
    }
  }

  /**
   * Logs the given message with debug level.
   *
   * @param message message to be logged.
   */
  public void debug(String message) {
    if (this.prefix != null) {
      this.logger.debug(this.prefix + message);
    } else {
      this.logger.debug(message);
    }
  }

  /**
   * Logs the given message with debug level.
   *
   * @param message message to be logged.
   * @param args    arguments to be logged.
   */
  public void debug(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.debug(this.prefix + message, args);
    } else {
      this.logger.debug(message, args);
    }
  }

  /**
   * Logs the given message with trace level.
   *
   * @param message message to be logged.
   */
  public void trace(String message) {
    if (this.prefix != null) {
      this.logger.trace(this.prefix + message);
    } else {
      this.logger.trace(message);
    }
  }

  /**
   * Logs the given message with trace level.
   *
   * @param message message to be logged.
   * @param args    arguments to be logged.
   */
  public void trace(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.trace(this.prefix + message, args);
    } else {
      this.logger.trace(message, args);
    }
  }

  /**
   * Logs the given message with error level.
   *
   * @param message message to be logged.
   */
  public void error(String message) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message);
    } else {
      this.logger.error(message);
    }
  }

  /**
   * Logs the given message with error level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   */
  public void error(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, t);
    } else {
      this.logger.error(message, t);
    }
  }

  /**
   * Logs the given message with error level.
   *
   * @param message message to be logged.
   * @param args    arguments to be used in the message.
   */
  public void error(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, args);
    } else {
      this.logger.error(message, args);
    }
  }

  /**
   * Logs the given message with error level.
   *
   * @param message message to be logged.
   * @param t       throwable to be logged.
   * @param args    arguments to be logged.
   */
  public void error(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, t, args);
    } else {
      this.logger.error(message, t, args);
    }
  }
}