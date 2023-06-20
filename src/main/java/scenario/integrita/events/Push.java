package scenario.integrita.events;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import network.packets.Event;
import scenario.integrita.historytree.HistoryTreeNode;

/**
 * Push event encapsulates a client-side push request to the server.
 */
public class Push implements Event {
  HistoryTreeNode historyTreeNode;
  String msg;

  public Push() {

  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, historyTreeNode")
  public Push(HistoryTreeNode historyTreeNode, String msg) {
    this.historyTreeNode = historyTreeNode;
    this.msg = msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "it is meant to access externally mutable object, historyTreeNode")
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
}
