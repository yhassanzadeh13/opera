package simulator;

import node.BaseNode;

public class Recipe {
  private final BaseNode baseNode;
  private final String nameSpace;
  private final short total;

  public Recipe(BaseNode baseNode, String nameSpace, short total) {
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

  public short getTotal() {
    return total;
  }
}
