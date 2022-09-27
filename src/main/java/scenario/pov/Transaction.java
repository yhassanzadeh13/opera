package scenario.pov;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import node.Identifier;

/**
 * Transaction is a serializable object which consist of uuid, owner, validators, and prevBlock.
 */
public class Transaction implements Serializable {
  private Identifier identifier;
  private Identifier owner;
  private List<Identifier> validators;
  private boolean collected;

  private Block prevBlock;

  /**
   * Constructor of Transaction.
   *
   * @param identifier Unique ID of the transaction.
   * @param owner Unique ID of the owner of the transaction.
   * @param prevBlock Unique ID of the previous Block.
   * @param validators list of unique ID of the validators of the transaction.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, validators")
  public Transaction(Identifier identifier, Identifier owner, Block prevBlock, List<Identifier> validators) {
    this.identifier = identifier;
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

  public Identifier getId() {
    return this.identifier;
  }

  public Identifier getOwner() {
    return this.owner;
  }

}
