package SkipGraph.packets;

import java.io.Serializable;

public class Response implements Serializable {

    public int flowId;

    public Response(int flowId) {
        this.flowId = flowId;
    }
}
