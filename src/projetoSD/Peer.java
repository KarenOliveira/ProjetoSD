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
    private static Scanner sc = new Scanner(System.in);

    public Peer() {
        System.out.println("EchoClient");
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Peer(String ip) {
        System.out.println("EchoClient");
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String sendEcho(Mensagem msg) {
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
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    System.out.println("MAin");
                    Peer client = new Peer(entradaA[2]);
                    Mensagem mensagem = new Mensagem();
                    mensagem.setAction(entradaA[0]);
                    mensagem.setFileList(getFilesListByFolder(entradaA[1]));
                    mensagem.setPortUdp(entradaA[3]);
                    List<String> portsTcp = new ArrayList<>();
                    for(int i = 4;i<entradaA.length;i++){
                        if(!entradaA[i].isEmpty()){
                            portsTcp.add(entradaA[i]);
                        }
                    }
                    Collections.sort(portsTcp);
                    mensagem.setPortTcp(portsTcp);
                    System.out.println("Received:"+client.sendEcho(mensagem));
                    client.close();
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            }
        }
    }
    public synchronized static List<String> getFilesListByFolder(String path){
        System.out.println("RecegetFilesListByFolderived:"+path);
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
}
