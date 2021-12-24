package simulator;

import node.BaseNode;

public class Recipe {
  private final BaseNode baseNode;
  private final String nameSpace;
  private final int total;

  public Recipe(BaseNode baseNode, String nameSpace, int total) {
    this.baseNode = baseNode;
    this.nameSpace = nameSpace;
    this.total = total;
  }

  public BaseNode getBaseNode() {
    return baseNode;
  }

  public String getNameSpace() {
    return nameSpace;
  }

  public int getTotal() {
    return total;
  }
}
