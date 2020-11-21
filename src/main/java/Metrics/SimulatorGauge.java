package Metrics;

import Simulator.Simulator;
import io.prometheus.client.Gauge;

import java.util.UUID;

/**
 * This class provides a prometheus-based Gauge for extracting metrics
 * For every registered metric, the metric will be collected for each node separately
 * Gauges can be incremented and decremented.
 */

public abstract class SimulatorGauge extends SimulatorMetric {

    public static boolean inc(String name, UUID id, double v){
        Gauge metric = getMetric(name);
        if(metric == null)return false;
        metric.labels(id.toString()).inc(v);
        return true;
    }
    public static boolean inc(String name, UUID id){
        return inc(name, id, 1.0);
    }

    public static boolean dec(String name, UUID id, double v){
        Gauge metric = getMetric(name);
        if(metric == null)return false;
        metric.labels(id.toString()).dec(v);
        return true;
    }

    public static boolean dec(String name, UUID id){
        return dec(name, id, 1.0);
    }

    public static boolean set(String name, UUID id, double v){
        Gauge metric = getMetric(name);
        if(metric == null)return false;
        metric.labels(id.toString()).set(v);
        return true;
    }

    public static double get(String name, UUID id){
        Gauge metric = getMetric(name);
        if(metric == null)return 0;
        return metric.labels(id.toString()).get();
    }

    /**
     * Return prometheus metric for a specific name
     * @param name
     * @return the requested metric
     */
    public static Gauge getMetric(String name){
        if(!collectors.containsKey(name)){
            Simulator.getLogger().error("[SimulatorGauge] could not find a metric with name " + name);
            System.err.println("[SimulatorGauge] could not find a metric with name " + name);
            return null;
        }
        if(collectorsTypes.get(name) != TYPE.GAUGE){
            Simulator.getLogger().error("[SimulatorGauge] metric registered with the name " + name +" is not a Gauge");
            System.err.println("[SimulatorGauge] metric registered with the name " + name +" is not a Gauge");
            return null;
        }
        return (Gauge) collectors.get(name);
    }

    /**
     * Register a new metric with a specific name
     * @param name
     * @return True in case of success
     */
    public static boolean register(String name){
        if(!collectors.containsKey(name)){
            collectors.put(name, Gauge.build().namespace(NAMESPACE).name(name).help(HELP_MSG).labelNames(LABEL_NAME).register());
            collectorsTypes.put(name, TYPE.GAUGE);
        }else{
            if(collectorsTypes.get(name) != TYPE.GAUGE){
                System.err.println("[SimulatorGauge] Collector name is already registered with a different type " + collectorsTypes.get(name));
                Simulator.getLogger().error("[SimulatorGauge] Collector name is already registered with a different type " + collectorsTypes.get(name));
                return false;
            }
        }
        return true;
    }

}
