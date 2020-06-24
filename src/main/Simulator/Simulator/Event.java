package Simulator;

import java.io.Serializable;


public interface Event extends Serializable {
    void actionPerformed();
    String logMessage();
}
