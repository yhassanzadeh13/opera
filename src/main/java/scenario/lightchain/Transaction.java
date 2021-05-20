package scenario.lightchain;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Transaction implements Serializable {

  private UUID uuid;
  private UUID owner;
  private List<UUID> validators;
  private boolean collected;

  private Block prevBlock;

  public Transaction(UUID uuid, UUID owner, Block prevBlock, List<UUID> validators) {

    this.uuid = uuid;
    this.owner = owner;
    this.prevBlock = prevBlock;
    this.validators = validators;
    this.collected = false;
  }

  public boolean isCollected() {
    return this.collected;
  }

  public void collect() {
    this.collected = true;
  }

  public UUID getID() {
    return this.uuid;
  }

  public UUID getOwner() {
    return this.owner;
  }

}
