package projetoSD;
import java.net.*;
public class Servidor extends Thread{
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024];

    public Servidor() {
        try {
            socket = new DatagramSocket(10098);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        running = true;

        while (running) {
            try {
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String msgDecode  = new String(buf, "UTF-8");
                msgDecode = "BAH"+msgDecode;
                byte[] msgDecodeBytes = msgDecode.getBytes();
                packet = new DatagramPacket(msgDecodeBytes, msgDecodeBytes.length, address, port);
                System.out.println(new String(packet.getData(),"UTF-8"));
                socket.send(packet);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public static void main(String[] args) {
        new Servidor().start();
    }
}
