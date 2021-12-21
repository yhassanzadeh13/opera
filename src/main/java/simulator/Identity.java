package simulator;

import java.util.UUID;

public class Identity {
  private final UUID identifier;
  private final String type;

  public Identity(UUID identifier, String type) {
    this.identifier = identifier;
    this.type = type;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public String getType() {
    return type;
  }
}
