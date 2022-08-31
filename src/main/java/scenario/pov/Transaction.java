package scenario.pov;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Transaction is a serializable object which consist of uuid, owner, validators, and prevBlock.
 */
public class Transaction implements Serializable {
  private UUID uuid;
  private UUID owner;
  private List<UUID> validators;
  private boolean collected;

  private Block prevBlock;

  /**
   * Constructor of Transaction.
   *
   * @param uuid Unique ID of the transaction.
   * @param owner Unique ID of the owner of the transaction.
   * @param prevBlock Unique ID of the previous Block.
   * @param validators list of unique ID of the validators of the transaction.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, validators")
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

  public UUID getId() {
    return this.uuid;
  }

  public UUID getOwner() {
    return this.owner;
  }

}
