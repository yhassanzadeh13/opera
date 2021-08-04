package simulator;

import java.util.UUID;

public interface Orchestrator {
  void ready(UUID nodeId);

  void done(UUID nodeId);

  int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional);
}
