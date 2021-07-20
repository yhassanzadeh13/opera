package SimulatorExamples.BullyAlgorithm;

import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Underlay.MiddleLayer;
import java.util.Collections;
import Underlay.packets.Event;
import Simulator.Simulator;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class MyNode implements BaseNode{
    public static Logger log = Logger.getLogger(Simulator.class.getName());

    public Simulator simulator;
    private UUID selfID;
    public ArrayList<UUID> allID;
    private MiddleLayer network;
    public UUID coordinatorID;

    public MyNode(){}
    MyNode(UUID selfID, MiddleLayer network){
        this.selfID = selfID;
        this.network = network;

        //Register metrics
        SimulatorHistogram.register("packetSize", new double[]{1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 20.0});
    }

    /**
     * Compares selfID with maxID on the allID list.
     * @return whether this node has maximum UUID or not
     */
    public boolean isMax(){
        UUID maxUUID = Collections.max(allID);
        return maxUUID == this.selfID;
    }

    /** Send Message
     *  Sends "Victory" message to all other nodes if this node has maxID and the coordinator is not determined yet
     *  Else sends "Election" message to nodes which has bigger ID than selfID
     */
    public void SendMessage(){
        if(this.isMax()){
            log.info("CoordinatorID: " + selfID);
            for (UUID targetID : allID){
                log.info(selfID + " sends to" + targetID + " " + "Victory Message.");
                Message victoryMassage = new Message("Victory", selfID, targetID);
                network.send(targetID, victoryMassage);
            }
        }
        else{
            for (UUID targetID : allID){
                if (targetID.compareTo(selfID) == 1) {
                    log.info(selfID + " sends to" + targetID + " " + "Election Message.");
                    Message electionMassage = new Message("Election", selfID, targetID);
                    network.send(targetID, electionMassage);
                }
            }
        }
    }

    /** Set allID
     * @param allIDs list of all ID's
     */
    public void setAllID(ArrayList<UUID> allIDs){this.allID = allIDs;}

    /** Get MaxID
     * @return maxID in the allID list
     */
    public UUID getMaxID(){return(Collections.max(allID));}

    /** Get Coordinator's ID
     * @return ID of the coordinator
     */
    public UUID getCoordinatorID(){return(this.coordinatorID);}

    /** Get SelfID
     * @return ID of this node
     */
    public UUID getUUID(){return(this.selfID);}

    /** set UUID
     * @param uuid to change uuid of the node
     */
    public void setUUID(UUID uuid){this.selfID = uuid;}

    /** Set CoordinatorID
     * @param uuid to set the coordinator ID
     */
    public void setCoordinatorID(UUID uuid){
        this.coordinatorID = uuid;
    }

    /** Set Simulation
     * @param simulator to set the simulator
     */
    public void setSimulation(Simulator simulator){
        simulator = simulator;
        allID = simulator.getAllID();
        coordinatorID = Collections.max(allID);
    }


    @Override
    public void onCreate(ArrayList<UUID> allID) {
        this.allID = allID;
        network.ready();
    }

    @Override
    public void onStart() {
        if (this.coordinatorID == null){
            this.SendMessage();
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public BaseNode newInstance(UUID ID, MiddleLayer network) {
        return new MyNode(ID, network);
    }

    @Override
    public void onNewMessage(UUID originID, Event msg){
        coordinatorID=this.getMaxID();
        try{
            Random rand = new Random();
            Thread.sleep(rand.nextInt(1000));
        }catch (InterruptedException e){log.error("Interruption");}
        msg.actionPerformed(this);


    }


}
