package simulator;

import java.util.UUID;

/**
 * Orchestrator is an interface for Simulator.
 * ready: Called when Orchestrator is ready to operate.
 * done: Called when Orchestrator done with its operation.
 * getSimulatedLatency: Getter for Orchestrator network.latency.
 */
public interface Orchestrator {
  void ready(UUID nodeId);

  void done(UUID nodeId);
}
