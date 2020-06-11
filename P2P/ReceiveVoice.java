import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReceiveVoice extends Voice {

    private final int packetSize = 1000;
    private int port;
    private DatagramSocket socket;

    public ReceiveVoice(int port) {
        this.port = port;
    }

    private void initSocket() {

        try {
            this.socket = new DatagramSocket(this.port);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void run() {


        initSocket();
        // Create a packet
        DatagramPacket packet = new DatagramPacket(new byte[this.packetSize], (this.packetSize));
        //setup audio outputs, from parent class
        try {
            this.setupOutput();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                this.socket.receive(packet);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // Play the audio
            this.getSourceDataLine().write(packet.getData(), 0, this.packetSize);
            packet.setLength(this.packetSize);
        }

    }

    public static void main(String[] args) {

        // Check the whether the arguments are given
        if (args.length != 1) {
            System.out.println("peer ip Required");
            return;
        }

        final int sendPort = 3575;
        final int receivePort = 55001;

        SendVoice sendVoice;
        ReceiveVoice receiveVoice;

        try {

            //create the thread for sending packets
            sendVoice = new SendVoice(InetAddress.getByName(args[0]), sendPort, receivePort);
            sendVoice.start(); // start the thread

            //create thread for receiving packets
            receiveVoice = new ReceiveVoice(receivePort);
            receiveVoice.start();//start receiving packets

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}