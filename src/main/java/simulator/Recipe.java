package simulator;

import node.BaseNode;

/**
 * Recipe is utilized by node factory to create a certain number of a specific type of node
 * for simulation.
 */
public class Recipe {
    private final BaseNode baseNode;
    private final String nameSpace;
    private final int total;

    /**
     * Constructor of recipe.
     *
     * @param baseNode  instance of a base node.
     * @param nameSpace (optional) string to virtually group nodes with identical name space.
     * @param total     total number of node instances of this type.
     */
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
