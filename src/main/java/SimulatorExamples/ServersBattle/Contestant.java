package SimulatorExamples.ServersBattle;

import Metrics.SimulatorCounter;
import Metrics.SimulatorGauge;
import Metrics.SimulatorHistogram;
import Node.BaseNode;
import Simulator.Simulator;
import Underlay.MiddleLayer;
import Underlay.packets.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class Contestant implements BaseNode {

    private UUID selfId;
    private ArrayList<UUID> allID;
    public boolean isFighting;
    public boolean isWaiting;
    private int healthLevel;
    ReentrantLock lock = new ReentrantLock();
    MiddleLayer network;

    static final String FIGHTCOUNT = "FightCount";
    static final String FIGHTDURATION = "FightDuration";
    static final String HEALTHLEVEL = "HealthLevel";

    Contestant(){}
    Contestant(UUID selfId, MiddleLayer network)
    {
        this.selfId = selfId;
        this.network = network;

        //Register metrics
        SimulatorGauge.register(HEALTHLEVEL);
        SimulatorHistogram.register(FIGHTDURATION, new double[]{500.0, 1000.0, 1500.0, 2000.0, 2500.0});
        SimulatorCounter.register(FIGHTCOUNT);
    }

    public UUID getId()
    {
        return this.selfId;
    }

    boolean isFighting()
    {
        return this.isFighting;
    }

    int getHealthLevel()
    {
        return this.healthLevel;
    }

    @Override
    public void onCreate(ArrayList<UUID> allID) {

        Random rand = new Random();
        this.healthLevel = rand.nextInt(30) + 1;
        Simulator.getLogger().info("Contestant " + this.selfId + "was initialized with level " + this.healthLevel);
        this.isFighting = false;
        this.isWaiting = false;
        this.allID = allID;
        network.ready();
    }

    @Override
    public void onStart() {
        this.sendNewFightInvitation();
    }

    @Override
    public void onStop() {
        System.out.println("Contestant " + this.selfId + " says goodbye");
    }

    @Override
    public void onNewMessage(UUID originID, Event msg) {
        msg.actionPerformed(this);
    }

    @Override
    public BaseNode newInstance(UUID selfID, MiddleLayer network) {return new Contestant(selfID, network);}

    public synchronized void sendNewFightInvitation()
    {
        if(this.allID.size() <=  1){
            if(!this.allID.isEmpty() && allID.get(0).equals(this.selfId)){
                Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
                System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
            }
            return;
        }
        Random rand = new Random();
        int duration = rand.nextInt(2000) + 500;
        this.isWaiting = false;
        Collections.shuffle(this.allID);
        int ind = 0;
        while(!this.isWaiting && !this.isFighting)
        {
            UUID targetId = this.allID.get(ind);
            if(!this.selfId.equals(targetId)) {
                if(!network.send(targetId, new BattleInvitation(this.selfId, targetId, duration)))
                    this.allID.remove(targetId);
                else {
                    this.isWaiting = true;

                    try{
                        Thread.sleep(100);
                    }
                    catch (Exception e)
                    {
                        Simulator.getLogger().error(e.getMessage());
                    }
                }
            }
            if(this.allID.size() <=  1){
                if(!this.allID.isEmpty() && allID.get(0).equals(this.selfId))
                    Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
                return;
            }
            ind += 1;
            if(ind == allID.size())
            {
                try{
                    Thread.sleep(500);
                }catch (Exception e)
                {
                    Simulator.getLogger().error("Thread cannot sleep" + e.getMessage());
                }
                ind = 0;
            }
        }
    }

    public void onNewFightInvitation(UUID host, int duration)
    {
        if(!this.isFighting) {
            if(network.send(host, new BattleConfirmation(host, this.selfId, true, duration, this.healthLevel)))
                this.isFighting = true;
            else
                Simulator.getLogger().debug(this.selfId + "could not reach " + host);
        }
        else
            network.send(host, new BattleConfirmation(host, this.selfId, false));

    }

    public synchronized void hostFight(UUID opponent, int opponentLevel, int duration){
        if(this.isFighting)
            network.send(opponent, new BattleResult(this.selfId, opponent, true));

        else if (opponentLevel > 0 && this.healthLevel > 0){
            lock.lock();
            try {
                this.isFighting = true;
            }
            finally {
                lock.unlock();
            }

            Simulator.getLogger().info(this.selfId + " is fighting against " + opponent);
            try {
                Thread.sleep(duration);
            }
            catch (Exception e) {
                Simulator.getLogger().error(e.getMessage());
            }

            int res = 0;
            if(this.healthLevel > opponentLevel) res = 1;
            else if(this.healthLevel < opponentLevel) res = -1;
            System.out.println("New fighting is happening between contestant with level " + opponentLevel + " and contestant with level " +  this.healthLevel);
            network.send(opponent, new BattleResult(this.selfId, opponent, false, res * -1));
            // update metrics
            SimulatorCounter.inc(FIGHTCOUNT, this.selfId);
            SimulatorCounter.inc(FIGHTCOUNT, opponent);
            SimulatorHistogram.observe(FIGHTDURATION, this.selfId, duration);
            SimulatorHistogram.observe(FIGHTDURATION, opponent, duration);
            updateHealth(res);
        }
    }

    public synchronized void updateHealth(int result)
    {
        switch (result){
            case -1:
                this.healthLevel -= 10;
                SimulatorGauge.dec(HEALTHLEVEL, this.selfId, 10);
                Simulator.getLogger().info(this.selfId + " losses 10 points");
                break;
            case 0:
                this.healthLevel += 1;
                SimulatorGauge.inc(HEALTHLEVEL, this.selfId, 1);
                Simulator.getLogger().info(this.selfId + " gains 1 point");
                break;
            case 1:
                this.healthLevel += 5;
                SimulatorGauge.inc(HEALTHLEVEL, this.selfId, 5);
                Simulator.getLogger().info(this.selfId + " gains 5 points");
                break;
        }
        if(this.healthLevel <= 0)
            {network.done(); return;}

        this.isFighting = false;
        this.isWaiting = false;
        if(this.allID.size() <=  1){
            if(!this.allID.isEmpty() && allID.get(0).equals(this.selfId)){
                Simulator.getLogger().info("Contestant " + this.selfId + " is the winner");
                System.out.println("Congrats. Contestant " + this.selfId + " is the winner");
            }
            return;
        }

        sendNewFightInvitation();
    }


}
