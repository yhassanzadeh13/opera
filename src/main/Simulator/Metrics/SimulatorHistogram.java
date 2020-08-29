package Metrics;

import Simulator.Simulator;
import io.prometheus.client.Histogram;

import java.util.*;

public abstract class SimulatorHistogram extends SimulatorMetric{

    private static HashMap<String, HashMap<String, ArrayDeque<Histogram.Timer>> >timersByName = new HashMap<>();

    public static boolean observe(String name, UUID id, double v){
        Histogram metric = getMetric(name);
        if(metric == null)return false;
        metric.labels(id.toString()).observe(v);
        return true;
    }

    public static boolean startTimer(String name, UUID id, String timerID){
        System.out.println("start ID " + timerID);
        Histogram metric = getMetric(name);
        if(metric == null)return false;

        if(!timersByName.containsKey(name))
            timersByName.put(name, new HashMap<>());

        if(timersByName.get(name).get(timerID) == null){
            timersByName.get(name).put(timerID, new ArrayDeque<>());
        }

        timersByName.get(name).get(timerID).addLast(metric.labels(id.toString()).startTimer());
        return true;
    }


    public static boolean observeDuration(String name, UUID id, String timerID){
        try {
            System.out.println("end ID " + timerID);
            timersByName.get(name).get(timerID).getFirst().observeDuration();
            timersByName.get(name).get(timerID).removeFirst();
            return true;
        }
        catch (Exception e){
            Simulator.getLogger().error("[HistogramSimulator] timer is not initialized");
            System.err.println("[HistogramSimulator] timer is not initialized");
            return false;
        }
    }

    public static boolean register(String name){
        return register(name, new double[]{0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.0});
    }

    public static boolean register(String name, double[] buckets){
        if(!collectors.containsKey(name)){
            collectors.put(name, Histogram.build().buckets(buckets).namespace(NAMESPACE).name(name).help(HELP_MSG).labelNames(LABEL_NAME).register());
            collectorsTypes.put(name, TYPE.HISTOGRAM);
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
