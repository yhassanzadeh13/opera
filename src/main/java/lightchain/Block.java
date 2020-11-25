package lightchain;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Block implements Serializable {

  private UUID uuid;
  private UUID owner;
  private List<UUID> validators;
  private Integer height;
  private UUID prevBlock;

  public Block(UUID uuid, Integer height, UUID owner, UUID prevBlock, List<UUID> validators) {
    this.uuid = uuid;
    this.height = height;
    this.owner = owner;
    this.prevBlock = prevBlock;
    this.validators = validators;
  }

  public UUID getID() {
    return this.uuid;
  }

  public UUID getOwner() {
    return this.owner;
  }

  public Integer getHeight() {
    return this.height;
  }
}
