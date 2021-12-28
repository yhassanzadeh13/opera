package scenario.integrita.historytree;


import java.util.Arrays;

public class HistoryTreeNode {
    NodeAddress addr;
    String op;
    byte[] hash;
    byte[] signature;

    @Override
    public String toString() {
        return "HistoryTreeNode{" +
                "addr=" + addr +
                ", op='" + op + '\'' +
                ", hash=" + Arrays.toString(hash) +
                ", signature=" + Arrays.toString(signature) +
                '}';
    }
}
