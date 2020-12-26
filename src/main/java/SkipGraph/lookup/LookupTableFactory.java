package SkipGraph.lookup;

public class LookupTableFactory {
    // TODO Move these perhaps to a settings file of some sort
    public static final String DEFAULT_FACTORY_TYPE = "Lookup";

    public static LookupTable createDefaultLookupTable(int numLevels) {
        return createLookupTable(DEFAULT_FACTORY_TYPE, numLevels);
    }

    public static LookupTable createLookupTable(String type, int tableSize) {
        if (type.equals("Backup")) {
            return new ConcurrentBackupTable(tableSize);
        }
        return new ConcurrentLookupTable(tableSize);
    }
}
