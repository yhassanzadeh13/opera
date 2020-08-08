package SimulatorExamples.ServersBattle;

import Node.BaseNode;
import underlay.MiddleLayer;
import underlay.packets.Event;
import Simulator.Simulator;

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
    MiddleLayer netowrk;

    Contestant(UUID selfId, MiddleLayer netowrk)
    {
        this.selfId = selfId;
        this.netowrk = netowrk;
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
        netowrk.ready();
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
    public BaseNode newInstance(UUID selfID, MiddleLayer netowrk) {return new Contestant(selfID, netowrk);}

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
                if(!netowrk.send(targetId, new BattleInvitation(this.selfId, targetId, duration)))
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
            if(netowrk.send(host, new BattleConfirmation(host, this.selfId, true, duration, this.healthLevel)))
                this.isFighting = true;
            else
                Simulator.getLogger().debug(this.selfId + "could not reach " + host);
        }
        else
            netowrk.send(host, new BattleConfirmation(host, this.selfId, false));

    }

    public synchronized void hostFight(UUID opponent, int opponentLevel, int duration){

        if(this.isFighting)
            netowrk.send(opponent, new BattleResult(this.selfId, opponent, true));

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
            netowrk.send(opponent, new BattleResult(this.selfId, opponent, false, res * -1));
            updateHealth(res);
        }
    }

    public synchronized void updateHealth(int result)
    {
        switch (result){
            case -1:
                this.healthLevel -= 10;
                Simulator.getLogger().info(this.selfId + " losses 10 points");
                break;
            case 0:
                this.healthLevel += 1;
                Simulator.getLogger().info(this.selfId + " gains 1 point");
                break;
            case 1:
                this.healthLevel += 5;
                Simulator.getLogger().info(this.selfId + " gains 5 points");
                break;
        }
        if(this.healthLevel <= 0)
            {netowrk.done(); return;}

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
