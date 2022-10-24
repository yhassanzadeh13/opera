package scenario.pov;

import java.io.Serializable;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import node.Identifier;

/**
 * Block representation in Proof-of-Validation.
 */
public class Block implements Serializable {
  private final Identifier identifier;
  private final Identifier owner;
  private final List<Identifier> validators;
  private final Integer height;
  private final Identifier prevBlock;
  private final List<Identifier> transactions;

  /**
   * Constructor of the block object.
   *
   * @param identifier   unique identifier of the block.
   * @param height       height of the block.
   * @param owner        Unique identifier of the owner of the block.
   * @param prevBlock    Unique identifier of the previous block.
   * @param validators   List of unique identifiers of the validators.
   * @param transactions List of unique identifiers of the transactions.
   */
  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to expose internal state of network")
  public Block(Identifier identifier, Integer height, Identifier owner, Identifier prevBlock, List<Identifier> validators, List<Identifier> transactions) {
    this.identifier = identifier;
    this.height = height;
    this.owner = owner;
    this.prevBlock = prevBlock;
    this.validators = validators;
    this.transactions = transactions;
  }

  public Identifier getId() {
    return this.identifier;
  }

  public Identifier getOwner() {
    return this.owner;
  }

  public Integer getHeight() {
    return this.height;
  }

  public Identifier getPrev() {
    return this.prevBlock;
  }
}
