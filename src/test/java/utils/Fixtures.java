package utils;

import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

import network.FixtureNode;
import network.NetworkProtocol;
import network.Underlay;
import network.UnderlayFactory;
import node.Identifier;
import node.IdentifierGenerator;

public class Fixtures {
  /**
   * Test fixture for creating and returning identifier list.
   *
   * @param count number of identifiers.
   * @return identifier list.
   */
  public static ArrayList<Identifier> identifierListFixture(int count) {
    ArrayList<Identifier> allId = new ArrayList<>();
    while (allId.size() != count) {
      allId.add(IdentifierGenerator.newIdentifier());
    }

    return allId;
  }

  /**
   * Creates list of fixture nodes with specified underlay.
   *
   * @param underlayName underlay protocol name.
   * @param count        total number of nodes.
   * @return list of created fixture nodes.
   */
  public static ArrayList<network.FixtureNode> nodeListFixture(NetworkProtocol underlayName, int count) {
    ArrayList<network.FixtureNode> nodes = new ArrayList<>();
    ArrayList<Identifier> allId = identifierListFixture(count);
    HashMap<Identifier, InetSocketAddress> allFullAddresses = new HashMap<>();

    for (int i = 0; i < count; i++) {
      Identifier id = allId.get(i);

      network.Network network = new network.Network(id, allFullAddresses, new NoopOrchestrator());

      network.FixtureNode node = new FixtureNode(id, allId, network);
      network.setNode(node);
      Underlay underlay = UnderlayFactory.newUnderlay(underlayName, 0, network);
      assert underlay != null;
      allFullAddresses.put(id, new InetSocketAddress(underlay.getAddress(), underlay.getPort()));
      network.setUnderlay(underlay);

      nodes.add(node);
    }

    return nodes;
  }
}
