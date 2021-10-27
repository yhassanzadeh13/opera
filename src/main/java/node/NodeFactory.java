package node;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import underlay.MiddleLayer;

/**
 *
 */
public class NodeFactory {
  Map<BaseNode, Integer> nodeMap;
  int numNodes;
  int ind;

  public NodeFactory() {
    nodeMap = new LinkedHashMap<BaseNode, Integer>();
    numNodes = 0;
    ind = 0;
  }

  public void put(BaseNode node, int n) {
    nodeMap.put(node, n);
    numNodes += n;
  }

  public int size() {
    return numNodes;
  }

  /**
   * .
   *
   * @param selfId
   * @param network
   * @return
   */
  public BaseNode newInstance(UUID selfId, MiddleLayer network) {
    Iterator<Map.Entry<BaseNode, Integer>> it = nodeMap.entrySet().iterator();
    if (it.hasNext()) {
      Map.Entry pair = it.next();
      BaseNode node = (BaseNode) pair.getKey();
      String nodeType = node.getClass().getName();
      int count = (int) pair.getValue();
      if (count == 1) {
        it.remove();
      } else {
        nodeMap.put(node, count - 1);
      }
      System.out.println(nodeType);
      return node.newInstance(selfId, network);
    }
    return null;
  }
}