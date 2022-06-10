package projetoSD;
import java.net.*;
public class Servidor extends Thread{
    private DatagramSocket socket;
    private boolean running;

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
                byte[] buf = new byte[1024];
                DatagramPacket packet
                        = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
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
