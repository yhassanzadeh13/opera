package utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import metrics.NoopCollector;
import network.*;
import network.FixtureNode;

public class Fixtures {
  /**
   * Test fixture for creating and returning identifier list.
   * @param count number of identifiers.
   * @return identifier list.
   */
  public static ArrayList<UUID> identifierListFixture(int count) {
    ArrayList<UUID> allId = new ArrayList<>();
    while (allId.size() != count) {
      allId.add(UUID.randomUUID());
    }

    return allId;
  }

  /**
   * Creates list of fixture nodes with specified underlay.
   * @param underlayName underlay protocol name.
   * @param count total number of nodes.
   * @return list of created fixture nodes.
   */
  public static ArrayList<network.FixtureNode> nodeListFixture(UnderlayType underlayName, int count) {
    ArrayList<network.FixtureNode> nodes = new ArrayList<>();
    ArrayList<UUID> allId = identifierListFixture(count);
    HashMap<UUID, AbstractMap.SimpleEntry<String, Integer>> allFullAddresses = new HashMap<>();
    HashMap<AbstractMap.SimpleEntry<String, Integer>, Boolean> isReady = new HashMap<>();


    for (int i = 0; i < count; i++) {
      UUID id = allId.get(i);

      MiddleLayer middleLayer = new MiddleLayer(id,
          allFullAddresses,
          isReady,
          new NoopOrchestrator(),
          new NoopCollector());

      network.FixtureNode node = new FixtureNode(id, allId, middleLayer);
      middleLayer.setOverlay(node);
      Underlay underlay = UnderlayFactory.newUnderlay(underlayName, 0, middleLayer);
      assert underlay != null;
      int port = underlay.getPort();
      allFullAddresses.put(id, new AbstractMap.SimpleEntry<>(underlay.getAddress(), port));
      middleLayer.setUnderlay(underlay);

      nodes.add(node);
    }

    return nodes;
  }
}
