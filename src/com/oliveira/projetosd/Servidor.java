package com.oliveira.projetosd;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class Servidor {
    private DatagramSocket socketProcess;
    private DatagramSocket socketAlive;
    private int PORT = 10098;
    private int PORT_ALIVE = 10099;
    private static final int sleepTime = 30000;
    Map<String, List<String>> fileByServer = new ConcurrentHashMap<>();

    public Servidor() {
        boolean error;
        String entrada;
        InetAddress addressServer = null;
        Scanner sc = new Scanner(System.in);
        do {
            error = false;
            try {
                System.out.println("Digite IP do Server");
                entrada = sc.nextLine();
                addressServer = InetAddress.getByName(entrada);
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
        new Servidor();
    }
    //Cada thread é responsável por escutar um determinado protocolo lembrando que a variável funcao é um String que indica qual protocolo deve ser escutado
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
            //Esse if é responsável por escutar o Socket UDP e Delegar para outra thread fazer o processamento (Thread PROCESAR-UDP)
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
            //Esse if é responsável por orquestrar o ALIVE e Delegar para outra thread fazer o processamento (Thread SEND-ALIVE)
            } else if (this.funcao.equals("ALIVE")) {
                while (running) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fileByServer.keySet()
                            .stream()
                            .forEach(key -> {
                                new ServidorThread("SEND-ALIVE", key).start();
                            });
                }
            //Esse if é o responsável por enviar o ALIVE aos Peers
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
                        System.out.println("Peer " + url + " morto. Eliminando seus arquivos ["+String.join(";", fileByServer.get(url))+"]");
                        fileByServer.remove(url);
                    }
                } catch (final Exception e) {
                    System.out.println("Peer " + url + " morto. Eliminando seus arquivos ["+String.join(";", fileByServer.get(url))+"]");
                    fileByServer.remove(url);
                }
            //Esse if é o responsável por processar as requisições UDP que chegam ao servidor, a Action da mensagem enviada é que define o processamento
            } else if (this.funcao.equals("PROCESSAR-UDP")) {
                Mensagem mensagem = new Mensagem(buf);
                if (mensagem.getAction().equals("JOIN")) {
                    String peerString = Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.put(peerString, mensagem.getFileList());
                    Mensagem retorno = new Mensagem("JOIN_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                    System.out.println("Peer " + peerString + " adicionado com arquivos: " + String.join(",", mensagem.getFileList()));
                } else if (mensagem.getAction().equals("LEAVE")) {
                    String peerString = Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem);
                    fileByServer.remove(peerString);
                    Mensagem retorno = new Mensagem("LEAVE_OK");
                    buf = new Gson().toJson(retorno).getBytes();
                } else if (mensagem.getAction().equals("SEARCH")) {
                    System.out.println("Peer: "+mensagem.getPeerUrl()+" solicitou arquivo: ["+mensagem.getFileName()+"]");
                    List<String> listPeers = fileByServer.entrySet()
                            .stream()
                            .filter(item -> item.getValue().contains(mensagem.getFileName()))
                            .map(map -> map.getKey())
                            .collect(Collectors.toList());
                    Mensagem retorno = new Mensagem(listPeers);
                    buf = new Gson().toJson(retorno).getBytes();
                } else if (mensagem.getAction().equals("UPDATE")) {
                    List<String> fileList = fileByServer.get(Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem));
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                    }
                    fileList.add(mensagem.getFileName());
                    fileByServer.put(Mensagem.buildUrl(packet.getAddress().getHostAddress(), mensagem), fileList);
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