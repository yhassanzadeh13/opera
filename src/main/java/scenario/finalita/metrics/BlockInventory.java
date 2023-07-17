package scenario.finalita.metrics;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import node.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Collects the identifiers of unique blocks at each height.
 */
public class BlockInventory {
  private static final BlockInventory instance = new BlockInventory();
  private final HashMap<Integer, List<Identifier>> inventory;
  private final ReentrantLock lock;

  private BlockInventory() {
    this.inventory = new HashMap<>();
    this.lock = new ReentrantLock();
  }

  @SuppressFBWarnings(value = "MS_EXPOSE_REP", justification = "instance is exposed externally")
  public static BlockInventory getInstance() {
    return instance;
  }

  /**
   * Adds a block to the inventory.
   *
   * @param height block height.
   * @param id     block id.
   * @return true if there is no duplicate block with the same identifier at the same height, and false otherwise.
   */
  public boolean addBlock(int height, Identifier id) {
    lock.lock();
    try {
      if (!inventory.containsKey(height)) {
        inventory.put(height, new ArrayList<>());
      }
      if (inventory.get(height).contains(id)) {
        // Block already exists.
        return false;
      }
      inventory.get(height).add(id);
    } finally {
      lock.unlock();
    }
    return true;
  }

  /**
   * Returns the number of unique blocks at a given height.
   *
   * @param height the height to check.
   * @return the number of unique blocks at the given height.
   */
  public int getBlockCount(int height) {
    lock.lock();
    try {
      if (!inventory.containsKey(height)) {
        return 0;
      }
      return inventory.get(height).size();
    } finally {
      lock.unlock();
    }
  }
}
