package Utils;

import Simulator.Simulator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Local static variable between the nodes with a buffer size of 1 (i.e any two consecutive writing on the same variable is forbidden)
 */
public class SharedVariable {
    private static final ConcurrentHashMap<String, Object> variables = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> isRead = new ConcurrentHashMap<>();


    /**
     * write a new value to the variable with the given name.
     * In case the name is assigned with another type, the variable will be overwritten.
     * No two consecutive write is allowed.
     * @param name
     * @param variable
     * @return Ture in case of success, False otherwise.
     */
    public static boolean write(String name, Object variable){
        if(isRead.containsKey(name) && !isRead.get(name)) {
            Simulator.getLogger().debug("[SharedVariable] the variable with the name " + name + " has not been read yet since the last write");
            return false;
        }
        variables.put(name, variable);
        isRead.put(name, false);
        return true;
    }

    public static Object read(String name){
        if(!variables.containsKey(name)){
            Simulator.getLogger().debug("[SharedVariable] the variable with the name " + name + " was not found");
            return null;
        }
        isRead.put(name, true);
        return variables.get(name);
    }
}
