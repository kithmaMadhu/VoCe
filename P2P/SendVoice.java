import java.io.IOException;
import java.net.*;
import java.net.DatagramSocket;


public class SendVoice extends Voice {

    private final int packetSize = 1000;
    private int port;
    private InetAddress peer;
    private int peerPort;
    private DatagramSocket socket = null;
    private byte buffer[] = new byte[this.packetSize];

    public SendVoice(InetAddress peer, int port,int peerPort) {
        this.peer = peer;
        this.port = port;
        this.peerPort=peerPort;

    }

    private void send() {

        try {
            int count;
            for (; ; ) {
                count = this.getTargetDataLine().read(this.buffer, 0, this.buffer.length);  //capture sound into buffer
                if (Integer.signum(count) > 0) {
                    // Construct the packet
                    DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length, this.peer, this.peerPort);
                    // Send the packet
                    this.socket.send(packet);
//                    System.out.println("sent something");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        try {
            initSocket();
            //setup the audio input and output,function is in the parent class
            this.captureAudio();
            //start sending the packets
            this.send();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.socket.close();
        }
    }

    private void initSocket() throws SocketException {
        this.socket = new DatagramSocket(this.port);
    }


}