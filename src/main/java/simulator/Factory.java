package simulator;

import java.util.ArrayList;

public class Factory {
  private final ArrayList<Recipe> recipes;
  private int total;

  public Factory() {
    this.recipes = new ArrayList<>();
    this.total = 0;
  }

  public void AddRecipe(Recipe r) {
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
