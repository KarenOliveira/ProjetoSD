package projetoSD;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Peer  {
    private InetAddress address;
    private String ip;
    private int portUdp;
    private String peerUrl;
    private String path;
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
                    path = entradaA[1];
                    mensagem.setPeerUrl(peerUrl);
                    socket = new DatagramSocket(portUdp,address);
                    Mensagem retorno = sendEcho(mensagem);
                    if(retorno.getAction().equals("JOIN_OK")){
                        new PeerThread("ALIVE",socket).start();
                        portsTcp.stream().forEach(port -> {
                            try{
                                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
                                new PeerThread("ESCUTAR-TCP",serverSocket).start();
                            }catch(final Exception e){
                                System.out.println("Erro ao criar socket");
                                e.printStackTrace();
                            }
                        });
                    }
 
                }catch(final ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                } catch (UnknownHostException e) {
                    System.out.println("Servidor ["+ip+"] não encontrado");
                }catch(final BindException e){
                    System.out.println("Porta ["+portUdp+"] já está em uso");
                } catch (final IOException e){
                    System.out.println("O Path passado não é válido, favor verificar os Arquivos");
                } catch (Exception e){
                    System.out.println("Chegou aqui");
                }
            }else if(entrada.startsWith("LEAVE")){
                try{
                    String[] entradaA = entrada.split("\\s+",-1);
                    mensagem.setAction(entradaA[0]);
                    System.out.println("Received:"+sendEcho(mensagem));
                }catch(final ArrayIndexOutOfBoundsException e){
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            }else if(entrada.startsWith("SEARCH")){
                try{
                    String fileName = entrada.substring(entrada.indexOf(" ")+1);
                    mensagem.setAction("SEARCH");
                    mensagem.setFileName(fileName);
                   Mensagem listaPeers = sendEcho(mensagem);
                   Socket socket = null;
                    for(String peer: listaPeers.getPeerList()){
                        System.out.println("Tentando conectar ao peer: "+peer);
                        if(socket!=null){
                            break;
                        }
                        String ip = peer.split(";",-1)[0];
                        String portTcp = peer.split(";",-1)[2];
                        String[] tcpArray = portTcp.split(":",-1);
                        for(int i = 0;i<tcpArray.length;i++){
                            if(socket!=null){
                                break;
                            }
                            if(!tcpArray[i].isEmpty()){
                                try{
                                    System.out.println("Tentando conectar ao peer: "+ip+ " na porta: "+tcpArray[i]);
                                    socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(tcpArray[i]));
                                }catch(final Exception e){
                                    System.out.println("Erro ao criar socket");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(socket!=null){
                        new PeerThread("SEND-DOWNLOAD",socket,fileName).start();
                    }
                }catch(final ArrayIndexOutOfBoundsException e){
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
            Mensagem retorno = new Mensagem(packet.getData());
            return retorno;
        }catch(final Exception e){
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
        private DatagramSocket datagramSocket;
        private ServerSocket serverSocket;
        private Socket socket;
        private String fileName;

        public PeerThread(String funcao,ServerSocket serverSocket){
            this.funcao = funcao;
            this.serverSocket = serverSocket;
        }
        public PeerThread(String funcao,DatagramSocket datagramSocket){
            this.funcao = funcao;
            this.datagramSocket = datagramSocket;
        }
        public PeerThread(String funcao,Socket socket,String fileName){
            this.funcao = funcao;
            this.socket = socket;
            this.fileName = fileName;
        }
        public void run(){
            boolean running = true;
            if(this.funcao.equals("ALIVE")){
                while (running) {
                    try {
                        DatagramPacket packet
                                = new DatagramPacket(buf, buf.length);
                                datagramSocket.receive(packet);
                        Mensagem retorno = new Mensagem("ALIVE_OK");
                        buf = new Gson().toJson(retorno).getBytes();
                        packet = new DatagramPacket(buf, buf.length, address, packet.getPort());
                        datagramSocket.send(packet);              
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if(this.funcao.equals("ESCUTAR-TCP")){
                try{
                    //Initialize Sockets
                    Socket socket = serverSocket.accept();
                    //The InetAddress specification
                    //Specify the file
                    File file = new File("E:\\Bookmarks.txt");
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    //Get socket's output stream
                    OutputStream os = socket.getOutputStream();
                    //Read File Contents into contents array
                    byte[] contents;
                    long fileLength = file.length();
                    long current = 0;
                    while(current!=fileLength){
                    int size = 10000;
                    if(fileLength - current >= size)
                    current += size;
                    else{
                    size = (int)(fileLength - current);
                    current = fileLength;
                    }
                    contents = new byte[size];
                    bis.read(contents, 0, size);
                    os.write(contents);
                    System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
                    }
                    os.flush();
                    //File transfer done. Close the socket connection!
                    socket.close();
                    serverSocket.close();
                    System.out.println("File sent succesfully!");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(this.funcao.equals("SEND-DOWNLOAD")){
                System.out.println("Download");
                try{
                    String dado = "DOWNLOAD "+fileName;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    //No of bytes read in one read() call
                    bos.write(dado.getBytes());
                    bos.flush();
                    socket.close();
                    System.out.println("File saved successfully!");
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}