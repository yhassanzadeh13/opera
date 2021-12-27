package simulator;

import java.util.ArrayList;

/**
 * Factory supports creating several types of nodes each with a certain instances.
 */
public class Factory {
  private final ArrayList<Recipe> recipes;
  private int total;

  /**
   * Constructor of factory, creates an empty one.
   */
  public Factory() {
    this.recipes = new ArrayList<>();
    this.total = 0;
  }

  /**
   * Adds a recipe to this factory. A recipe contains a type of node as well
   * as the total instance of that type that must be created by factory for simulation.
   *
   * @param r recipe for a new node.
   */
  public void addRecipe(Recipe r) {
    this.recipes.add(r);
    this.total += r.getTotal();
  }

  public ArrayList<Recipe> getRecipes() {
    return recipes;
  }

  public int getTotalNodes() {
    return this.total;
  }
}
