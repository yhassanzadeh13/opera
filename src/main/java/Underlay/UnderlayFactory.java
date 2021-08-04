package underlay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import underlay.local.LocalUnderlay;


public class UnderlayFactory {

  private UnderlayFactory() {
  }

  private static Logger log = Logger.getLogger(UnderlayFactory.class.getName());

  // hashmap to hold the underlay types to underlay class names
  private static HashMap<String, String> underlayClassName;

  private static String yamlFile = "./src/main/java/underlay/underlayTypes.yml";

  public static LocalUnderlay getMockUnderlay(String address,
                                              int port,
                                              MiddleLayer middleLayer,
                                              HashMap<SimpleEntry<String, Integer>, LocalUnderlay> allLocalUnderlay) {
    LocalUnderlay underlay = new LocalUnderlay(address, port, allLocalUnderlay);
    underlay.initialize(port, middleLayer);
    return underlay;
  }

  /**
   * get a new underlay instance.
   *
   * @param underlayName the underlay type name according to underlayTypes yaml file
   * @param port
   * @param middleLayer
   * @return new underlay instance according to the given type
   */

  public static Underlay newUnderlay(UnderlayType underlayName, int port, MiddleLayer middleLayer) {
    // obtain underlay class name from the yaml file
    if (underlayClassName == null) {
      underlayClassName = readyaml();
    }

    // create new instance of underlay according to the class name
    try {
      String className = underlayClassName.get(underlayName.label);
      Underlay underLay = (Underlay) Class.forName(className).getConstructor().newInstance();
      underLay.initialize(port, middleLayer);
      return underLay;
    } catch (NullPointerException e) {
      System.err.println(
            "[UnderlayFactory] could not find underlay class name according to the given type " + underlayName);
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      System.err.println("[UnderlayFactory] could not create new underlay instance of type " + underlayName);
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Read the underlay types and corresponding classes names and store them in a hashmap.
   */
  private static HashMap<String, String> readyaml() {
    String yamlFile = "src/main/java/underlay/underlayTypes.yml";
    try {
      // obtain underlay class name from the yaml file
      Yaml yaml = new Yaml();
      InputStream inputStream = new FileInputStream(yamlFile);
      return yaml.load(inputStream);
    } catch (FileNotFoundException e) {
      System.err.println("[UnderlayFactory] could not open " + yamlFile);
      e.printStackTrace();
      return null;
    }
  }

}
