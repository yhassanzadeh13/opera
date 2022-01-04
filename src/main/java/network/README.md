# underlay

This package includes the implementation of common network protocols: 
TCP, UDP, JavaRMI, and a mock, thread-wise, network layer.

In order to add a new network protocol, extends the super class 'underlay' and 
add the corresponding class to the `underlayTypes.yml` file, which contains all
underlay's classes and type name which can later be passed to the simulator.
 