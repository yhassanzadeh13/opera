package node;

import java.util.UUID;

public class Identity {
  private final UUID identifier;
  private final String nameSpace;

  public Identity(UUID identifier, String nameSpace) {
    this.identifier = identifier;
    this.nameSpace = nameSpace;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public String getNameSpace() {
    return nameSpace;
  }
}
