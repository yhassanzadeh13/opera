package metrics;

public class Constants {
  public static final String PACKET_SIZE = "packet_size";
  public static final String UUID = "uuid";

  public class Namespace {
    public static final String SIMULATOR = "simulator";
    public static final String NETWORK = "network";
    public static final String DEMO = "demo";
  }

  public class Subsystem {
    public static final String SIMULATOR = "simulator";
  }

  public class Simulator {
    public class Name {
      public static final String SESSION_LENGTH = "SessionLength";
      public static final String INTER_ARRIVAL = "InterArrival";
    }

    public class HelpMsg {
      public static final String SESSION_LENGTH = "session length of nodes based on churn distribution";
      public static final String INTER_ARRIVAL = "inter arrival time of nodes based on churn distribution";
    }

  }

  public class Network {
    public static final String MIDDLELAYER = "middlelayer";

    public class Name {
      public static final String DELAY = "delay";
      public static final String MESSAGE_SENT_TOTAL = "message_sent_total";
      public static final String MESSAGE_RECEIVED_TOTAL = "message_received_total";
      public static final String PACKET_SIZE = "packet_size";
    }

    public class HelpMsg {
      public static final String DELAY = "inter-node latency";
      public static final String MESSAGE_SENT_TOTAL = "total messages sent";
      public static final String MESSAGE_RECEIVED_TOTAL = "total messages received";
      public static final String PACKET_SIZE = "size of exchanged packet size";
    }
  }

  public class Demo {
    public class Subsystem {
      public static final String SERVER_BATTLE = "server_battle";
    }


    public class ServersBattle {
      public class Name {
        public static final String FIGHT_TOTAL = "fight_total";
        public static final String SERVER_HEALTH = "server_health";
        public static final String FIGHT_DURATION = "fight_duration";
      }

      public class HelpMsg {
        public static final String FIGHT_TOTAL = "total number of fights a node made";
        public static final String SERVER_HEALTH = "level of health for a server";
        public static final String FIGHT_DURATION = "duration of fight for a server";
      }
    }
  }

  public class Histogram {
    public static final double[] DEFAULT_HISTOGRAM = new double[]{Double.MAX_VALUE};
  }
}
