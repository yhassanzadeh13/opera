package Metrics;

import Simulator.Simulator;
import io.prometheus.client.Histogram;

import java.util.*;

/**
 * This class provides a prometheus-based Histogram for extracting metrics
 * For every registered metric, the metric will be collected for each node separately
 */

public abstract class SimulatorHistogram extends SimulatorMetric{

    private static HashMap<String, HashMap<String, ArrayDeque<Histogram.Timer>> >timersByName = new HashMap<>();

    /**
     * Record a new value for the histogram with a specific name and id
     * @param name
     * @param id
     * @param v
     * @return
     */
    public static boolean observe(String name, UUID id, double v){
        Histogram metric = getMetric(name);
        if(metric == null)return false;
        metric.labels(id.toString()).observe(v);
        return true;
    }

    /**
     * Start a time for recoding a duration and add it to the histogram with a specific name, and timer ID. The time recording finishes once
     * the observeDuration method is called with the same name, and timer ID. The time elapsed will be recorded under the node with the given id.
     * In case of starting two timers with the same timer ID, the observeDuration method will stop the earliest one.
     * @param name
     * @param id
     * @param timerID
     * @return
     */
    public static boolean startTimer(String name, UUID id, String timerID){
        Histogram metric = getMetric(name);
        if(metric == null)
            return false;

        if(!timersByName.containsKey(name))
            timersByName.put(name, new HashMap<>());

        if(!timersByName.get(name).containsKey(timerID)){
            timersByName.get(name).put(timerID, new ArrayDeque<>());
        }
        try {
            timersByName.get(name).get(timerID).addLast(metric.labels(id.toString()).startTimer());
        }catch (NullPointerException e){
            Simulator.log.error("[SimulatorHistogram] NullPointerException " + e.getMessage());
        }
        Simulator.getLogger().debug("[SimulatorHistogram] Timer of name " + name + " and id " + timerID + " has started at time " + System.currentTimeMillis());
        return true;
    }

    /**
     * Observe the duration of the already started timer with a specific name and timer ID. to start a timer, please use the startTimer method.
     * @param name
     * @param timerID
     * @return
     */
    public static boolean observeDuration(String name, String timerID){
        try {
            timersByName.get(name).get(timerID).getFirst().observeDuration();
            timersByName.get(name).get(timerID).removeFirst();
            Simulator.getLogger().debug("[SimulatorHistogram] duration of name " + name + " and id " + timerID + " was observed at time " + System.currentTimeMillis());
            return true;
        }
        catch (Exception e){
            Simulator.getLogger().error("[SimulatorHistogram] timer with name " + name + " and ID " + timerID + " is not initialized");
            return false;
        }
    }

    public static boolean register(String name){
        return register(name, new double[]{0.005, 0.01, 0.05, 0.1, 0.15, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.1});
    }

    /**
     * Register a new Histogram with given buckets
     * @param name
     * @param buckets
     * @return
     */
    public static boolean register(String name, double[] buckets){
        if(!collectors.containsKey(name)){
            collectors.put(name, Histogram.build().buckets(buckets).namespace(NAMESPACE).name(name).help(HELP_MSG).labelNames(LABEL_NAME).register());
            collectorsTypes.put(name, TYPE.HISTOGRAM);
            Simulator.getLogger().info("[SimulatorHistogram] Collector with name " + name + " was registered");
        }else{
            if(collectorsTypes.get(name) != TYPE.HISTOGRAM){
                System.err.println("[SimulatorHistogram] Collector name is already registered with a different type " + collectorsTypes.get(name));
                Simulator.getLogger().error("[SimulatorHistogram] Collector name is already registered with a different type " + collectorsTypes.get(name));
                return false;
            }
        }
        return true;
    }

    public static Histogram getMetric(String name){
        if(!collectors.containsKey(name)){
            Simulator.getLogger().error("[SimulatorHistogram] could not find a metric with name " + name);
            System.err.println("[SimulatorHistogram] could not find a metric with name " + name);
            return null;
        }
        if(collectorsTypes.get(name) != TYPE.HISTOGRAM){
            Simulator.getLogger().error("[SimulatorHistogram] metric registered with the name " + name +" is not a Histogram");
            System.err.println("[SimulatorHistogram] metric registered with the name " + name +" is not a Histogram");
            return null;
        }
        return (Histogram) collectors.get(name);
    }


}
