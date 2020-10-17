# Simulator Metrics

This package includes three essential metrics types
implemented using [Prometheus](https://prometheus.io/).

### Registering new metric
All metric classes are static and registering new metric
can be done by calling the 'register' method and passing 
the metric name, and the `UUID` of the node, which will be used
a label in Prometheus metrics. 

### Running Prometheus
Configure Prometheus with the provided config file `prometheus.yml`
```
sudo prometheus --config.file=prometheus.yml
```

### Using [Grafana](https://grafana.com/)
