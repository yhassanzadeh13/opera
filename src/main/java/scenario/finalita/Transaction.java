package scenario.finalita;


import java.io.Serializable;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import node.Identifier;

/**
 * Transaction representation in Proof-of-Validation.
 */
public class Transaction implements Serializable {
  private final Identifier identifier;
  private final Identifier owner;
  private final List<Identifier> validators;
  private final Block prevBlock;
  private boolean collected;

  /**
   * Constructor of Transaction.
   *
   * @param identifier Unique identifier of the transaction.
   * @param owner      Unique identifier of the owner of the transaction.
   * @param prevBlock  Unique identifier of the previous Block.
   * @param validators list of unique identifier of the validators of the transaction.
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
