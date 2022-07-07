package projetoSD;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Servidor {
    
    private  DatagramSocket socket;
    public String ip;

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
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println("ALIVE-COMEÇANDO");
                    System.out.println("Size keySet: "+fileByServer.keySet().size());
                    fileByServer.keySet()
                        .stream()
                        .forEach(key->{
                            new ServidorThread("SEND-ALIVE",key).start();
                    });   
                    System.out.println("ALIVE-TERMINOU");
                }
            }
            else if(this.funcao.equals("SEND-ALIVE")){
                System.out.println("SEND ALIVE FOR:"+url);
            }
            else if(this.funcao.equals("PROCESSAR-UDP")){
                Mensagem mensagem = new Mensagem(buf);
                if(mensagem.getAction().equals("JOIN")){
                    StringBuilder sb = new StringBuilder();
                    sb.append(packet.getAddress().getHostAddress());
                    sb.append(";");
                    sb.append(mensagem.getPortUdp());
                    sb.append(";");
                    sb.append(String.join(":", mensagem.getPortsTcp()));
                    String peerString = sb.toString();
                    fileByServer.put(peerString, mensagem.getFileList());
                    System.out.println("Lista:"+new Gson().toJson(fileByServer));
                    Mensagem retorno  = new Mensagem("JOIN_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                }
                else if(mensagem.getAction().equals("LEAVE")){
                    StringBuilder sb = new StringBuilder();
                    sb.append(packet.getAddress().getHostAddress());
                    sb.append(";");
                    sb.append(mensagem.getPortUdp());
                    sb.append(";");
                    sb.append(String.join(":", mensagem.getPortsTcp()));
                    String peerString = sb.toString();
                    fileByServer.remove(peerString);
                    //System.out.println("Lista:"+new Gson().toJson(fileByServer));
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