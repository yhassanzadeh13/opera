package utils;

import node.Identifier;
import simulator.Orchestrator;


/**
 * NoopOrchestrator implements an orchestrator for testing.
 * It is assumed not to perform any operation.
 */
public class NoopOrchestrator implements Orchestrator {

  @Override
  public void ready(Identifier nodeId) {

  }

  @Override
  public void done(Identifier nodeId) {

  }
}
