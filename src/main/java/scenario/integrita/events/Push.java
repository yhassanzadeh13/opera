package scenario.integrita.events;

import network.packets.Event;
import node.BaseNode;
import scenario.integrita.historytree.HistoryTreeNode;

/**
 * Push event encapsulates a client-side push request to the server.
 */
public class Push implements Event {
  HistoryTreeNode historyTreeNode;
  String msg;

  public Push() {

  }

  public Push(HistoryTreeNode historyTreeNode, String msg) {
    this.historyTreeNode = historyTreeNode;
    this.msg = msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public void setHistoryTreeNode(HistoryTreeNode historyTreeNode) {
    this.historyTreeNode = historyTreeNode;
  }

  @Override
  public String toString() {
    return "Push{"
            + "historyTreeNode=" + historyTreeNode
            + ", msg='" + msg + '\''
            + '}';
  }

  @Override
  public boolean actionPerformed(BaseNode hostNode) {
    return false;
  }

  @Override
  public String logMessage() {
    return this.toString();
  }

  @Override
  public int size() {
    return 0;
  }
}
