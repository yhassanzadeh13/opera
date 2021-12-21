package simulator;

import java.util.Hashtable;
import java.util.Iterator;

import node.BaseNode;

public class Factory {
  private Hashtable<BaseNode, Short> inventory;
  private int total;

  public Factory(){
    this.inventory = new Hashtable<>();
    this.total = 0;
  }

  public void AddRecipe(BaseNode node, short number){
      this.inventory.put(node, number);
      this.total += number;
  }

  public Hashtable<BaseNode, Short> getInventory() {
    return this.inventory;
  }

  public int getTotalNodes(){
    return this.total;
  }
}
