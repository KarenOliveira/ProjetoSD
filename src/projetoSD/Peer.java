package projetoSD;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
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

    public String sendEcho(Mensagem msg) {
        byte[] buf = new byte[1024];
        System.out.println("sendEcho");
        try{
        DatagramPacket packet
                = new DatagramPacket(new Gson().toJson(msg).getBytes(), new Gson().toJson(msg).getBytes().length, address, 10098);
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
                    Peer client = new Peer();
                    Mensagem mensagem = new Mensagem();
                    mensagem.setAction(entradaA[0]);
                    mensagem.setIp(entradaA[1]);
                    mensagem.setPort(entradaA[2]);
                    mensagem.setFileFolder(entradaA[3]);
                    mensagem.setFileList(getFilesListByFolder(entradaA[3]));
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
