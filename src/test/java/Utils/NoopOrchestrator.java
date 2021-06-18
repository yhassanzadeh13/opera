package Utils;

import Simulator.Orchestrator;

import java.util.UUID;

/**
 * NoopOrchestrator implements an orchestrator for testing.
 * It is assumed not to perform any operation.
 */
public class NoopOrchestrator implements Orchestrator {

    @Override
    public void Ready(UUID nodeId) {

    }

    @Override
    public void Done(UUID nodeId) {

    }

    @Override
    public int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional) {
        return 0;
    }
}
