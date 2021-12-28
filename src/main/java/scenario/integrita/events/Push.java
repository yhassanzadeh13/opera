package scenario.integrita.events;

import node.BaseNode;
import scenario.integrita.historytree.HistoryTreeNode;
import underlay.packets.Event;

public class Push implements Event {
    HistoryTreeNode historyTreeNode;
    String msg;

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setHistoryTreeNode(HistoryTreeNode historyTreeNode) {
        this.historyTreeNode = historyTreeNode;
    }

    @Override
    public boolean actionPerformed(BaseNode hostNode) {
        return false;
    }

    @Override
    public String logMessage() {
        return msg;
    }

    @Override
    public int size() {
        return 0;
    }
}
