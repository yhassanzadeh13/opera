package Metrics;

import io.prometheus.client.*;

import java.util.HashMap;


public class SimulatorMetric {

    enum TYPE{
        COUNTER,
        GAUGE,
        HISTOGRAM,
        SUMMARY
    }
    protected static final HashMap<String, Collector> collectors = new HashMap<>();
    protected static final HashMap<String, TYPE> collectorsTypes = new HashMap<>();
    protected static final String NAMESPACE = "simulator";
    protected static final String LABEL_NAME = "uuid";
    protected static final String HELP_MSG = "opera native metric";

}
