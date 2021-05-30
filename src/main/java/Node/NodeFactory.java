package Node;


import Underlay.MiddleLayer;

import java.util.*;

public class NodeFactory {
    Map<BaseNode, Integer> nodeMap;
    int numNodes;
    int ind;

    public NodeFactory(){
        nodeMap = new HashMap<BaseNode, Integer>();
        numNodes = 0;
        ind = 0;
    }

    public void put(BaseNode node, int n){
        nodeMap.put(node, n);
        numNodes += n;
    }

    public int size(){
        return numNodes;
    }

    public BaseNode newInstance(UUID selfID, MiddleLayer network){
        Iterator it = nodeMap.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            BaseNode node = (BaseNode) pair.getKey();
            String nodeType = node.getClass().getName();
            int count = (int) pair.getValue();
            if (count == 1)
                it.remove();
            else
                nodeMap.put(node, count-1);
            System.out.println(nodeType);
            return node.newInstance(selfID, network);
        }
        return null;
    }
}
