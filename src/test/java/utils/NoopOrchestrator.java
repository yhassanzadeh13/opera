package utils;

import java.util.UUID;
import simulator.Orchestrator;


/**
 * NoopOrchestrator implements an orchestrator for testing.
 * It is assumed not to perform any operation.
 */
public class NoopOrchestrator implements Orchestrator {

  @Override
  public void ready(UUID nodeId) {

  }

  @Override
  public void done(UUID nodeId) {

  }

  @Override
  public int getSimulatedLatency(UUID nodeA, UUID nodeB, boolean bidirectional) {
    return 0;
  }
}
