package projetoSD;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Peer  {
    private InetAddress address;
    private String ip;
    private int portUdp;
    private String peerUrl;
    private static Scanner sc = new Scanner(System.in);

    public Peer() {
        String entrada;
        Mensagem mensagem = new Mensagem();
        while (true){
            System.out.println("Digite Comando");
            entrada = sc.nextLine();
            if(entrada.startsWith("JOIN")){
                DatagramSocket socket = null;
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction(entradaA[0]);
                    mensagem.setFileList(getFilesListByFolder(entradaA[1]));
                    List<String> portsTcp = new ArrayList<>();
                    for(int i = 4;i<entradaA.length;i++){
                        if(!entradaA[i].isEmpty()){
                            portsTcp.add(entradaA[i]);
                        }
                    }
                    Collections.sort(portsTcp);
                    ip = entradaA[2];
                    address = InetAddress.getByName(entradaA[2]);
                    portUdp = Integer.parseInt(entradaA[3]);
                    peerUrl = Mensagem.buildUrl(ip, portUdp, portsTcp);
                    mensagem.setPeerUrl(peerUrl);
                    System.out.println("porta: "+portUdp+"add: "+address.getHostAddress());
                    socket = new DatagramSocket(portUdp,address);
                    Mensagem retorno = sendEcho(mensagem);
                    if(retorno.getAction().equals("JOIN_OK")){
                        System.out.println("Peer conectado com sucesso");
                        new PeerThread("ALIVE",socket).start();
                        portsTcp.stream().forEach(port -> {
                            try{
                                //System.out.println("Conectando ao porto: " + port);
                            //ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
                            //new PeerThread("ESCUTAR-TCP",serverSocket).start();
                            }catch(Exception e){
                                System.out.println("Erro ao criar socket");
                                e.printStackTrace();
                            }
                        });
                    }
 
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                } catch (UnknownHostException e) {
                    System.out.println("Servidor ["+ip+"] não encontrado");
                }catch(BindException e){
                    System.out.println("Porta ["+portUdp+"] já está em uso");
                }
                catch (IOException e){
                    e.printStackTrace();
                    System.out.println("O Path passado não é válido, favor verificar os Arquivos");
                } catch (Exception e){
                    System.out.println("Chegou aqui");
                }finally{
                    if(socket != null){
                        //socket.close();
                    }
                }
            }else if(entrada.startsWith("LEAVE")){
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction(entradaA[0]);
                    System.out.println("Received:"+sendEcho(mensagem));
                }catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            }else if(entrada.startsWith("SEARCH")){
                try{
                    String fileName = entrada.substring(entrada.indexOf(" ")+1);
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
        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();
            Gson gson = new GsonBuilder()
            .setLenient()
            .create();
            DatagramPacket packet
                    = new DatagramPacket(gson.toJson(msg).getBytes(), gson.toJson(msg).getBytes().length, address, 10098);
            socket.send(packet);
            socket.setSoTimeout(5000);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet); 
            return new Mensagem(packet.getData());
        }catch(Exception e){
            e.printStackTrace();
            return new Mensagem();
        }finally
        {   
            if(socket != null){
                socket.close();
            }
        }
    }
    public static void main(String[] args) {
        new Peer();
    }
    public synchronized static List<String> getFilesListByFolder(String path) throws IOException{
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
           return paths
                .filter(Files::isRegularFile)
                .map(fileItem -> fileItem.getFileName().toString())
                .collect(Collectors.toList());
        } catch(final IOException e){
            e.printStackTrace();
            throw new IOException();
        }
    }
    class  PeerThread extends Thread{
        private String funcao;
        private byte[] buf = new byte[1024];
        private DatagramSocket socket;
        private ServerSocket serverSocket;

        public PeerThread(String funcao,ServerSocket serverSocket){
            this.funcao = funcao;
            this.serverSocket = serverSocket;
        }
        public PeerThread(String funcao,DatagramSocket socket){
            System.out.println("Funcao "+ funcao+" Conectado ao porto: "+socket.getLocalPort());
            this.funcao = funcao;
            this.socket = socket;
        }
        public void run(){
            boolean running = true;
            if(this.funcao.equals("ALIVE")){
                System.out.println("ALIVE");
                while (running) {
                    try {
                        DatagramPacket packet
                                = new DatagramPacket(buf, buf.length);
                        System.out.println("Recebendo...");
                        socket.receive(packet);
                        Mensagem mensagem = new Mensagem(packet.getData());
                        System.out.println("Recebido: "+mensagem.getAction());
                        Mensagem retorno = new Mensagem("ALIVE_OK");
                        buf = new Gson().toJson(retorno).getBytes();
                        packet = new DatagramPacket(buf, buf.length, address, 10098);
                        socket.send(packet);              
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if(this.funcao.equals("ESCUTAR-TCP")){
                while (running) {
                    //System.out.println("ESCUTAR-TCP"+serverSocket.getLocalPort());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}