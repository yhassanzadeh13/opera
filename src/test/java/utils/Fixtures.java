package utils;

import java.util.ArrayList;
import java.util.UUID;

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
}
