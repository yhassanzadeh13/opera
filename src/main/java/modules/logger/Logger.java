package modules.logger;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Logger {
  private String prefix;
  private final org.slf4j.Logger logger;
  private final Marker fatal = MarkerFactory.getMarker("FATAL");

  public Logger(org.slf4j.Logger logger) {
    this.logger = logger;
  }

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
  public void fatal(String message) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message);
    } else {
      this.logger.error(this.fatal, message);
    }
    System.exit(1);
  }

  public void fatal(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, t);
    } else {
      this.logger.error(this.fatal, message, t);
    }

    System.exit(1);
  }

  public void fatal(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, args);
    } else {
      this.logger.error(this.fatal, message, args);
    }

    System.exit(1);
  }

  public void fatal(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.fatal, this.prefix + message, t, args);
    } else {
      this.logger.error(this.fatal, message, t, args);
    }

    System.exit(1);
  }

  public void warn(String message) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message);
    } else {
      this.logger.warn(message);
    }
  }

  public void warn(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, t);
    } else {
      this.logger.warn(message, t);
    }
  }

  public void warn(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, args);
    } else {
      this.logger.warn(message, args);
    }
  }

  public void warn(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.warn(this.prefix + message, t, args);
    } else {
      this.logger.warn(message, t, args);
    }
  }

  public void info(String message) {
    if (this.prefix != null) {
      this.logger.info(this.prefix + message);
    } else {
      this.logger.info(message);
    }
  }

  public void info(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.info(this.prefix + message, args);
    } else {
      this.logger.info(message, args);
    }
  }

  public void debug(String message) {
    if (this.prefix != null) {
      this.logger.debug(this.prefix + message);
    } else {
      this.logger.debug(message);
    }
  }

  public void debug(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.debug(this.prefix + message, args);
    } else {
      this.logger.debug(message, args);
    }
  }

  public void trace(String message) {
    if (this.prefix != null) {
      this.logger.trace(this.prefix + message);
    } else {
      this.logger.trace(message);
    }
  }

  public void trace(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.trace(this.prefix + message, args);
    } else {
      this.logger.trace(message, args);
    }
  }

  public void error(String message) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message);
    } else {
      this.logger.error(message);
    }
  }

  public void error(String message, Throwable t) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, t);
    } else {
      this.logger.error(message, t);
    }
  }

  public void error(String message, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, args);
    } else {
      this.logger.error(message, args);
    }
  }

  public void error(String message, Throwable t, Object... args) {
    if (this.prefix != null) {
      this.logger.error(this.prefix + message, t, args);
    } else {
      this.logger.error(message, t, args);
    }
  }
}