import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.*;

public class ReceiveVoice extends Voice {

    private final int packetSize = 1008;
    private int port;
    private MulticastSocket socket;
    private InetAddress host;
    private int seq[] = new int[16];
    private int user;
//    private String key = "cipher"; //key for the decryption

    public ReceiveVoice(InetAddress host, int port, int user) {
        this.host = host;
        this.port = port;
        this.user = user;
    }

    private void initSocket() {

        try {
            this.socket = new MulticastSocket(this.port);

            //avoid hearing my voice again
            this.socket.setLoopbackMode(true);//avoid loop back

            //used for windows environments ,it enables the multicast
            this.socket.setBroadcast(true);
            //join the multicast group
            this.socket.joinGroup(this.host);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void run() {
        byte[] keyBytes = getKey().getBytes(Charset.forName("UTF-8"));
        initSocket();
        // Create a packet
        DatagramPacket packet = new DatagramPacket(new byte[this.packetSize], (this.packetSize));
        //setup audio outputs, from parent class
        try {
            this.setupOutput();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        for (; ; ) {
            try {
                this.socket.receive(packet);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (packet.getData() != null) { // do things when there is packet data

                //setup the packet decoder
                PacketDecoder PD = new PacketDecoder(packet.getData(), keyBytes);

                if (PD.user >= 0 && PD.user < 16) { // let only 16 users to connect

                    //accounting for the first received packet ,not too high ,and not too low
                    if (seq[PD.user] == 0 && PD.seq < 768) {
                        seq[PD.user] = PD.seq; //add sequence number to packet decoder
                    }
                    if (PD.seq > 0 && PD.seq - seq[PD.user] <= 500) { //if packet sequence does not deviate too much
                        // Play the audio
                        this.getSourceDataLine().write(PD.buffer, 0, this.packetSize - 8); // write only the audio data
                        seq[PD.user] = PD.seq;
                    } else {
                        //if packet sequence deviate too much then discard the packet
                        System.out.println("Discarding out of sequence packet: " + PD.seq + "th from the user : "+PD.user);
                    }
                }

            }

            packet.setLength(this.packetSize);
        }

    }

    public static void main(String[] args) {

        // Check the whether the arguments are given
        if (args.length != 2) {
            System.out.println("Multicast ip & user id required");
            return;
        }

        final int port = 3575;

        SendVoice sendVoice;
        ReceiveVoice receiveVoice;

        int userId = Integer.parseInt(args[1]);
        try {

            //create the thread for sending packets
            sendVoice = new SendVoice(InetAddress.getByName(args[0]), port, userId);
            sendVoice.start(); // start the thread

            //create thread for receiving packets
            receiveVoice = new ReceiveVoice(InetAddress.getByName(args[0]), port, userId);
            receiveVoice.start();//start receiving packets

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
