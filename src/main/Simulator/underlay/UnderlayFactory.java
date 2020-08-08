package underlay;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import underlay.Local.LocalUnderlay;
import underlay.TCP.TCPUnderlay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

public class UnderlayFactory {

    private UnderlayFactory(){}

    private static Logger log = Logger.getLogger(UnderlayFactory.class.getName());

    // hashmap to hold the underlay types to underlayclass names
    private static HashMap<String, String> underlayClassName;

    private static String yamlFile = "./src/main/Simulator/underlay/underlayTypes.yaml";


    public static Underlay getMockUnderlay(HashMap<Pair<String, Integer>, MiddleLayer> allMiddleLayers){
        Underlay underlay = new LocalUnderlay(allMiddleLayers);
        underlay.initialize(-1, null);
        return underlay;
    }
    /**
     * get a new underlay instance
     * @param underlayName the underlay type name according to underlayTypes yaml file
     * @param port
     * @param middleLayer
     * @return new underlay instance according to the given type
     */

    public static Underlay NewUnderlay(String underlayName, int port, MiddleLayer middleLayer) {
        // obtain underlay class name from the yaml file
        if (underlayClassName == null)
            underlayClassName = readYAML();

        // create new instance of underlay according to the class name
        try {
            String className = underlayClassName.get(underlayName);
            Underlay underLay = (Underlay) Class.forName(className).getConstructor().newInstance();
            underLay.initialize(port, middleLayer);
            return underLay;
        }
        catch (NullPointerException e)
        {
            System.err.println("[UnderlayFactory] could not find underlay class name according to the given type " + underlayName);
            e.printStackTrace();
            return null;
        }
        catch (Exception e)
        {
            System.err.println("[UnderlayFactory] could not create new underlay instance of type " + underlayName);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Read the underlay types and corresponding classes names and store them in a hashmap
     */
    private static HashMap<String, String> readYAML()
    {
        try {
            // obtain underlay class name from the yaml file
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(yamlFile);
            return yaml.load(inputStream);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("[UnderlayFactory] could not open " + yamlFile);
            e.printStackTrace();
            return null;
        }
    }

}
