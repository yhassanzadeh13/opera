package Simulator;

import java.util.UUID;

public interface Orchestrator {
    void Ready(UUID nodeId);
    void Done(UUID nodeId);
    int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional);
}
