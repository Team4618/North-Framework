package north.network;

import edu.wpi.first.wpilibj.Timer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static north.network.NetworkDefinitions.*;

public class Network {
    public static DatagramChannel channel;
    public static Thread netThread;
    public static AtomicBoolean netThreadRunning = new AtomicBoolean(true);  //TODO: when does this get used?
    public static HashMap<SocketAddress, ConnectedClient> connections = new HashMap<>();

    public static class ConnectedClient {
        public SocketAddress address;
        public double lastReceived;

        public ConnectedClient(SocketAddress address) {
            this.address = address;
            this.lastReceived = Timer.getFPGATimestamp();
        }

        public void send(ByteBuffer data) {
            try {
                channel.send(data, address);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static void init() {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(5801));
            netThread = new Thread(Network::tick);
            netThread.start();
            System.out.println("Network Thread: " + netThread.isAlive());
        } catch(Exception e) { e.printStackTrace(); }
    }

    //NOTE: this is synchronized so we can call send from any thread without worrying
    //TODO: this might race condition with the remove disconnected loop below
    public static synchronized void send(ByteBuffer data) {
        for(ConnectedClient client : connections.values()) {
            client.send(data);
        }
    }

    public static void HandlePacket(SocketAddress sender, ByteBuffer data) {
        byte type = data.get();
        switch(type) {
            case Ping: break;

            case Connect: {
                ConnectedClient client = new ConnectedClient(sender);
                connections.put(sender, client);

                ByteBuffer welcomePacket = createWelcomePacket();
                client.send(welcomePacket);

                //TODO: send current params
            } break;

            case SetParameter: {
                //TODO: set param
                //TODO: send current params
            } break;

            case SetState: {
                //TODO: set state
            } break;

            case UploadAutonomous: {

            } break;

            default:
                System.err.println("Invalid packet type " + type + " received");
        }

        //update lastReceived
        if(connections.containsKey(sender)) {
            ConnectedClient client = connections.get(sender);
            client.lastReceived = Timer.getFPGATimestamp();
        }
    }

    public static ByteBuffer buffer = ByteBuffer.allocate(16384);
    public static void tick() {
        while(netThreadRunning.get()) {
            try {
                //Handle & respond to incoming packets
                boolean hasPackets = true;

                while (hasPackets) {
                    buffer.clear();
                    SocketAddress sender = channel.receive(buffer);
                    if (sender == null) {
                        hasPackets = false;
                    } else {
                        HandlePacket(sender, buffer);
                    }
                }

                //check last ping times of connected things, remove from list if timed out
                Iterator<ConnectedClient> clients = connections.values().iterator();
                while(clients.hasNext()) {
                    ConnectedClient client = clients.next();
                    if((Timer.getFPGATimestamp() - client.lastReceived) > 5) {
                        System.out.println(client.address.toString() + " has disconnected");
                        clients.remove();
                    }
                }

                //send state
                ByteBuffer statePacket = createStatePacket();
                send(statePacket);

                Thread.sleep(100);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
