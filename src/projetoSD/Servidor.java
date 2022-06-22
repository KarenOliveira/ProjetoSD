package projetoSD;
import java.net.*;
import java.util.Scanner;
public class Servidor {
    private  DatagramSocket socket;
    public String ip;

    public Servidor(){
        boolean error;
        String entrada;
        InetAddress addressServer = null;
        Scanner sc = new Scanner(System.in);
        do{
            error = false;
            try{
                System.out.println("Digite IP do Server");
                entrada = sc.nextLine();
                 addressServer = InetAddress.getByName(entrada);
                socket = new DatagramSocket(10098,addressServer);
            }catch(Exception e){
                System.out.println("Erro no IP");
                error = true;
            }
            this.ip = addressServer.getHostAddress();
        sc.close();
        } while(error);
        new ServidorThread("ESCUTAR-UDP").start();
        //new ServidorThread("ALIVE").start();
    }

    public static void main(String[] args) {
        new Servidor();
    }
    class  ServidorThread extends Thread{
        private String funcao;
        private DatagramPacket packet;
        private byte[] buf = new byte[1024];
        public ServidorThread(String funcao){
            this.funcao = funcao;
        }
        public ServidorThread(String funcao, DatagramPacket packet, byte[] buf){
            this.funcao = funcao;
            this.packet = packet;
            this.buf = buf;
        }
        public void run(){
            boolean running = true;
            if(this.funcao.equals("ESCUTAR-UDP")){
                while (running) {
                    try {
                        DatagramPacket packet
                                = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        new ServidorThread("PROCESSAR-UDP",packet,this.buf).start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                socket.close();
            }if(this.funcao.equals("PROCESSAR-UDP")){
                try{
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
