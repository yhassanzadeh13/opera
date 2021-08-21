package scenario.pov;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Block is a serializable object which consist of uuid, owner, validators, height, prevBlock, and transactions.
 *
 */
public class Block implements Serializable {

  private UUID uuid;
  private UUID owner;
  private List<UUID> validators;
  private Integer height;
  private UUID prevBlock;
  private List<UUID> transactions;

  /** Constructor of the block object.
   *
   * @param uuid unique Id of the block.
   * @param height height of the block.
   * @param owner  Unique Id of the owner of the block.
   * @param prevBlock Unique Id of the previous block.
   * @param validators List of unique IDs of the validators.
   * @param transactions List of unique IDs of the transactions.
   */
  public Block(UUID uuid, Integer height, UUID owner, UUID prevBlock, List<UUID> validators, List<UUID> transactions) {
    this.uuid = uuid;
    this.height = height;
    this.owner = owner;
    this.prevBlock = prevBlock;
    this.validators = validators;
    this.transactions = transactions;
  }

  public UUID getId() {
    return this.uuid;
  }

  public UUID getOwner() {
    return this.owner;
  }

  public Integer getHeight() {
    return this.height;
  }

  public UUID getPrev() {
    return this.prevBlock;
  }
}
