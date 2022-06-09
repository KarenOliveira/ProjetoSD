package projetoSD;

import java.net.*;
import java.util.Scanner;

import com.google.gson.Gson;
public class Peer  {
    private DatagramSocket socket;
    private InetAddress address;
    private static Scanner sc = new Scanner(System.in);

    private byte[] buf = new byte[1024];

    public Peer() {
        System.out.println("EchoClient");
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String sendEcho(Mensagem msg) {
        System.out.println("sendEcho");
        System.out.println("bufleng1 "+buf.length);
        try{
        System.out.println("bufleng3 "+buf.length);
        DatagramPacket packet
                = new DatagramPacket(new Gson().toJson(msg).getBytes(), new Gson().toJson(msg).getBytes().length, address, 10098);
        socket.send(packet);
        System.out.println("bufleng4 "+buf.length);
        packet = new DatagramPacket(buf, buf.length);
        System.out.println("bufleng5 "+buf.length);
        socket.receive(packet);
        String received = new String(
                packet.getData(), 0, packet.getLength());
            return received;
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
        String entrada;
        while (true){
            System.out.println("Digite JOIN");
            entrada = sc.nextLine();
            if(entrada.startsWith("JOIN")){
                System.out.println("MAin");
                Peer client = new Peer();
                System.out.println("Received:"+client.sendEcho(new Mensagem(entrada)));
                client.close();
            }
        }
    }
}
