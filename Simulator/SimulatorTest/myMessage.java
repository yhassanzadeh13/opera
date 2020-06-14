package SimulatorTest;

import Simulator.Message;

public class myMessage extends Message {

    String msg;

    public myMessage(String msg)
    {
        super(msg);
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
