package projetoSD;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class Servidor {
    ///TODO DELETE THIS
    private static String SERVER_IP = "localhost";

    private DatagramSocket socketProcess;
    private DatagramSocket socketAlive;
    private int PORT = 10098;
    private int PORT_ALIVE = 10099;
    private static final int sleepTime = 30000;
    Map<String, List<String>> fileByServer = new ConcurrentHashMap<>();

    public Servidor(String serverIp) {
        boolean error;
        String entrada;
        InetAddress addressServer = null;
        Scanner sc = new Scanner(System.in);
        do {
            error = false;
            try {
                System.out.println("Digite IP do Server");
                //TODO UNCOMMMENT THIS
                //entrada = sc.nextLine();
                addressServer = InetAddress.getByName(serverIp);
                socketProcess = new DatagramSocket(PORT, addressServer);
                socketAlive = new DatagramSocket(PORT_ALIVE, addressServer);
            } catch (final Exception e) {
                e.printStackTrace();
                System.out.println("Erro no IP");
                error = true;
            }
            
        } while (error);
        
        new ServidorThread("ESCUTAR-UDP").start();
        new ServidorThread("ALIVE").start();
    }

    public static void main(String[] args) {
        new Servidor(SERVER_IP);
    }

    class ServidorThread extends Thread {
        private String funcao;
        private DatagramPacket packet;
        private String url;
        private byte[] buf = new byte[1024];

        public ServidorThread(String funcao) {
            this.funcao = funcao;
        }

        public ServidorThread(String funcao, DatagramPacket packet, byte[] buf) {
            this.funcao = funcao;
            this.packet = packet;
            this.buf = buf;
        }

        public ServidorThread(String funcao, String url) {
            this.funcao = funcao;
            this.url = url;
        }

        public void run() {
            boolean running = true;
            if (this.funcao.equals("ESCUTAR-UDP")) {
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socketProcess.receive(packet);
                        new ServidorThread("PROCESSAR-UDP", packet, this.buf).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                socketProcess.close();
            } else if (this.funcao.equals("ALIVE")) {
                while (running) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fileByServer.keySet()
                            .stream()
                            .forEach(key -> {
                                new ServidorThread("SEND-ALIVE", key).start();
                            });
                }
            } else if (this.funcao.equals("SEND-ALIVE")) {
                byte[] buf = new byte[1024];
                try {
                    String[] arrayKey = url.split(";", -1);
                    InetAddress address = InetAddress.getByName(arrayKey[0]);
                    byte[] send = new Gson().toJson(new Mensagem("ALIVE")).getBytes();
                    int port = Integer.parseInt(arrayKey[1]);
                    packet = new DatagramPacket(send, send.length, address, port);
                    socketAlive.send(packet);
                    socketAlive.setSoTimeout(10000);
                    packet = new DatagramPacket(buf, buf.length);
                    socketAlive.receive(packet);
                    Mensagem mensagem = new Mensagem(packet.getData());
                    if (!mensagem.getAction().equals("ALIVE_OK")) {
                        fileByServer.remove(url);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    fileByServer.remove(url);
                }
            } else if (this.funcao.equals("PROCESSAR-UDP")) {
                Mensagem mensagem = new Mensagem(buf);
                if (mensagem.getAction().equals("JOIN")) {
                    String peerString = Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.put(peerString, mensagem.getFileList());
                    System.out.println("Lista:" + new Gson().toJson(fileByServer));
                    Mensagem retorno = new Mensagem("JOIN_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                } else if (mensagem.getAction().equals("LEAVE")) {
                    String peerString = Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.remove(peerString);
                    Mensagem retorno = new Mensagem("LEAVE_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                } else if (mensagem.getAction().equals("SEARCH")) {
                    List<String> listPeers = fileByServer.entrySet()
                            .stream()
                            .filter(item -> item.getValue().contains(mensagem.getFileName()))
                            .map(map -> map.getKey())
                            .collect(Collectors.toList());
                    Mensagem retorno = new Mensagem(listPeers);
                    buf = new Gson().toJson(retorno).getBytes();
                } else if(mensagem.getAction().equals("UPDATE")){
                    System.out.println("UPDATE"+mensagem.toString());
                    List<String> fileList = fileByServer.get(mensagem.getPeerUrl());
                    if(fileList == null){
                        fileList = new ArrayList<>();
                    }
                    fileList.add(mensagem.getFileName());
                    fileByServer.put(Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem), fileList);
                    System.out.println("Lista:" + new Gson().toJson(fileByServer));
                    Mensagem retorno = new Mensagem("UPDATE_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                }
                try {
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socketProcess.send(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}