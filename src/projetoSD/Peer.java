package projetoSD;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Peer  {
    private DatagramSocket socket;
    private InetAddress address;
    private String ip;
    private static Scanner sc = new Scanner(System.in);

    public Peer() {
        System.out.println("EchoClient");
        try {
            socket = new DatagramSocket();
        } catch (Exception e){
            e.printStackTrace();
        }
        String entrada;
        Mensagem mensagem = new Mensagem();
        while (true){
            System.out.println("Digite JOIN");
            entrada = sc.nextLine();
            if(entrada.startsWith("JOIN")){
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction(entradaA[0]);
                    mensagem.setFileList(getFilesListByFolder(entradaA[1]));
                    mensagem.setPortUdp(entradaA[3]);
                    ip = entradaA[2];
                    address = InetAddress.getByName(entradaA[2]);
                    List<String> portsTcp = new ArrayList<>();
                    for(int i = 4;i<entradaA.length;i++){
                        if(!entradaA[i].isEmpty()){
                            portsTcp.add(entradaA[i]);
                        }
                    }
                    Collections.sort(portsTcp);
                    mensagem.setPortTcp(portsTcp);
                    Mensagem retorno = sendEcho(mensagem);
                    if(retorno.getAction().equals("JOIN_OK")){
                        System.out.println("ALIVE ABERTO");
                        //new PeerThread("ESCUTAR-ALIVE",mensagem).start();
                    }
                    
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                } catch (UnknownHostException e) {
                    System.out.println("Servidor ["+ip+"] não encontrado");
                }
            }
            else if(entrada.startsWith("LEAVE")){
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction(entradaA[0]);
                    System.out.println("Received:"+sendEcho(mensagem));
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            }
            else if(entrada.startsWith("SEARCH")){
                try{
                    String fileName = entrada.substring(entrada.indexOf(" ")+1);
                    System.out.println("FileName: "+fileName);
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction("SEARCH");
                    mensagem.setFileName(fileName);
                    System.out.println("Received:"+sendEcho(mensagem));
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            }
        }
    }

    public Mensagem sendEcho(Mensagem msg) {
        byte[] buf = new byte[1024];
        System.out.println("sendEcho");
        try{
            Gson gson = new GsonBuilder()
            .setLenient()
            .create();
            System.out.println("enviadndo: "+gson.toJson(msg));
            DatagramPacket packet
                    = new DatagramPacket(gson.toJson(msg).getBytes(), gson.toJson(msg).getBytes().length, address, 10098);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return new Mensagem(packet.getData());
        }catch(Exception e){
            e.printStackTrace();
            return new Mensagem();
        }
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
        new Peer();
    }
    public synchronized static List<String> getFilesListByFolder(String path){
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
           return paths
                .filter(Files::isRegularFile)
                .map(fileItem -> fileItem.getFileName().toString())
                .collect(Collectors.toList());
        } catch(IOException e){
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }
    class  PeerThread extends Thread{
        private String funcao;
        private Mensagem mensagem;
        private DatagramSocket socketUdp;
        private DatagramPacket packetUdp;
        private byte[] buf = new byte[1024];


        public PeerThread(String funcao,Mensagem mensagem){
            this.funcao = funcao;
            this.mensagem = mensagem;
        }
        public PeerThread(String funcao,Mensagem mensagem,DatagramSocket socketUdp,DatagramPacket packetUdp){
            this.funcao = funcao;
            this.mensagem = mensagem;
            this.socketUdp = socketUdp;
            this.packetUdp = packetUdp;
        }

        public void run(){
            boolean running = true;
            System.out.println("run");
            if(this.funcao.equals("ALIVE")){
                while (running) {
                    try {
                        InetAddress addressServer = InetAddress.getByName("localhost");
                        socket = new DatagramSocket(Integer.parseInt(mensagem.getPortUdp()),addressServer);
                        DatagramPacket packet
                                = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        System.out.println("PROCESSA-ALIVE");
                        try{
                            InetAddress address = packetUdp.getAddress();
                            int port = packetUdp.getPort();
                            Mensagem retorno = new Mensagem("ALIVE_OK");
                            buf = new Gson().toJson(retorno).getBytes();
                            packetUdp = new DatagramPacket(buf, buf.length, address, port);
                            socket.send(packetUdp);
                        }catch (Exception e){
                            e.printStackTrace();
                        }                  
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}