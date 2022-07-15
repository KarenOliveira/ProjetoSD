package projetoSD;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class Servidor {
    
    private  DatagramSocket socket;
    public String ip;
    private static final int sleepTime = 3000;

    Map<String, List<String>> fileByServer = new ConcurrentHashMap<>();
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
        new ServidorThread("ALIVE").start();
    }

    public static void main(String[] args) {
        new Servidor();
    }
    class  ServidorThread extends Thread{

       
        private String funcao;
        private DatagramPacket packet;
        private String url;
        private byte[] buf = new byte[1024];
        private Mensagem mensagem;
        public ServidorThread(String funcao){
            this.funcao = funcao;
        }
        public ServidorThread(String funcao, DatagramPacket packet, byte[] buf){
            this.funcao = funcao;
            this.packet = packet;
            this.buf = buf;
        }
        public ServidorThread(String funcao, String url){
            this.funcao = funcao;
            this.url = url;
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
            }
            else if(this.funcao.equals("ALIVE")){
                while(running){
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fileByServer.keySet()
                        .stream()
                        .forEach(key->{
                            new ServidorThread("SEND-ALIVE",key).start();
                    });   
                }
            }
            else if(this.funcao.equals("SEND-ALIVE")){
                byte[] buf = new byte[1024];
                try{
                    String[] arrayKey = url.split(";",-1);
                    InetAddress address = InetAddress.getByName(arrayKey[0]);
                    byte[] send = new Gson().toJson(new Mensagem("ALIVE")).getBytes();
                    int port = Integer.parseInt(arrayKey[1]);
                    packet = new DatagramPacket(send, send.length, address, port);
                    socket.send(packet);
                    packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    mensagem = new Mensagem(packet.getData());
                    if(!mensagem.getAction().equals("ALIVE_OK")){
                        fileByServer.remove(url);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else if(this.funcao.equals("PROCESSAR-UDP")){
                Mensagem mensagem = new Mensagem(buf);
                if(mensagem.getAction().equals("JOIN")){
                    String peerString = mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.put(peerString, mensagem.getFileList());
                    System.out.println("Lista:"+new Gson().toJson(fileByServer));
                    Mensagem retorno  = new Mensagem("JOIN_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                }
                else if(mensagem.getAction().equals("LEAVE")){
                    String peerString = mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.remove(peerString);
                    Mensagem retorno  = new Mensagem("LEAVE_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                }
                else if(mensagem.getAction().equals("SEARCH")){
                    System.out.println("Search: "+mensagem.getFileName());
                    List<String> listPeers = fileByServer.entrySet()
                        .stream()
                        .filter(item -> item.getValue().contains(mensagem.getFileName()))
                        .map(map -> map.getKey())
                        .collect(Collectors.toList());
                    Mensagem retorno  = new Mensagem(listPeers);
                    buf = new Gson().toJson(retorno).getBytes();
                }
                
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