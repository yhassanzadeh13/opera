package simulator;

import java.util.Hashtable;
import java.util.Iterator;

import node.BaseNode;

public class Factory {
  private Hashtable<BaseNode, Short> inventory;

  public Factory(){
    this.inventory = new Hashtable<>();
  }

  public void AddRecipe(BaseNode node, short number){
      this.inventory.put(node, number);
  }

  public Hashtable<BaseNode, Short> getInventory() {
    return this.inventory;
  }
}
