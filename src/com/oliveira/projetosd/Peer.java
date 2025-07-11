package com.oliveira.projetosd;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Peer {
    private InetAddress address;
    private String ip;
    private int portUdp;
    private String peerUrl;
    private List<String> fileList;
    private String path;
    private static Scanner sc = new Scanner(System.in);

    public Peer() {
        String entrada;
        Mensagem mensagem = new Mensagem();
        while (true) {
            System.out.println("Digite Comando:");
            entrada = sc.nextLine();
            // while responsável pela linha de comando
            if (entrada.startsWith("JOIN")) {
                DatagramSocket socket = null;
                try {
                    String[] entradaA = entrada.split("\\s+", -1);
                    mensagem.setAction(entradaA[0]);
                    path = entradaA[1];
                    fileList = getFilesListByFolder(path);
                    ip = entradaA[2];
                    address = InetAddress.getByName(entradaA[2]);
                    portUdp = Integer.parseInt(entradaA[3]);
                    mensagem.setFileList(fileList);
                    List<String> portsTcp = new ArrayList<>();
                    for (int i = 4; i < entradaA.length; i++) {
                        if (!entradaA[i].isEmpty()) {
                            portsTcp.add(entradaA[i]);
                        }
                    }
                    Collections.sort(portsTcp);
                    peerUrl = Mensagem.buildUrl(ip, portUdp, portsTcp);
                    mensagem.setPeerUrl(peerUrl);
                    socket = new DatagramSocket(portUdp, address);
                    Mensagem retorno = sendEcho(mensagem);
                    if (retorno.getAction().equals("JOIN_OK")) {
                        // Caso o retorno do JOIN seja JOIN_OK é criada a escuta para o ALIVE (UDP) e o
                        // ESCUTAR-TCP (TCP)
                        new PeerThread("ALIVE", socket).start();
                        portsTcp.stream().forEach(port -> {
                            try {
                                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
                                new PeerThread("ESCUTAR-TCP", serverSocket).start();
                            } catch (final Exception e) {
                                System.out.println("Erro ao criar socket");
                                e.printStackTrace();
                            }
                        });
                        System.out.println("Sou Peer [" + ip + "][" + portUdp + "] com arquivos: ["
                                + String.join(",", fileList) + "]");
                    }

                } catch (final ArrayIndexOutOfBoundsException e) {
                    System.out.println("Quantidade de Argumentos Inválido");
                } catch (UnknownHostException e) {
                    System.out.println("Servidor [" + ip + "] não encontrado");
                } catch (final BindException e) {
                    System.out.println("Porta [" + portUdp + "] já está em uso");
                } catch (final IOException e) {
                    System.out.println("O Path passado não é válido, favor verificar os Arquivos");
                } catch (Exception e) {
                    System.out.println("Chegou aqui");
                }
            } else if (entrada.startsWith("LEAVE")) {
                try {
                    String[] entradaA = entrada.split("\\s+", -1);
                    mensagem.setAction(entradaA[0]);
                    sendEcho(mensagem);
                } catch (final ArrayIndexOutOfBoundsException e) {
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            } else if (entrada.startsWith("SEARCH")) {
                try {
                    String fileName = entrada.substring(entrada.indexOf(" ") + 1);
                    mensagem.setAction("SEARCH");
                    mensagem.setFileName(fileName);
                    mensagem.setPeerUrl(peerUrl);
                    Mensagem listaPeers = sendEcho(mensagem);
                    List<String> peers = listaPeers.getPeerList();
                    System.out.println("Peers com o arquivo: " + fileName + " " + String.join(",", peers));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    System.out.println("Quantidade de Argumentos Inválido");
                }
            } else if (entrada.startsWith("DOWNLOAD")) {
                String[] entradaA = entrada.split("\\s+", -1);
                String ip = entradaA[1];
                String port = entradaA[2];
                String fileName = entrada.substring(entrada.indexOf(port) + port.length() + 1);
                Socket socket = null;
                try {
                    socket = new Socket(ip, Integer.parseInt(port));
                    new PeerThread("REQUEST-DOWNLOAD", fileName, socket, "[" + ip + "]:[" + port + "]").start();
                } catch (Exception e) {
                    System.out.println("Peer [" + ip + "][" + port + "] negou  o download");
                }

            }
        }
    }

    public Mensagem sendEcho(Mensagem msg) {
        byte[] buf = new byte[1024];
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            DatagramPacket packet = new DatagramPacket(gson.toJson(msg).getBytes(), gson.toJson(msg).getBytes().length,
                    address, 10098);
            socket.send(packet);
            socket.setSoTimeout(5000);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            Mensagem retorno = new Mensagem(packet.getData());
            return retorno;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Mensagem();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public static void main(String[] args) {
        new Peer();
    }

    public synchronized static List<String> getFilesListByFolder(String path) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(fileItem -> fileItem.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    // Cada thread é responsável por escutar um determinado protocolo lembrando que
    // a variável funcao é um String que indica qual protocolo deve ser escutado
    class PeerThread extends Thread {
        private String funcao;
        private byte[] buf = new byte[1024];
        private DatagramSocket datagramSocket;
        private ServerSocket serverSocket;
        private Socket socket;
        private String fileName;
        private String ipPort;

        public PeerThread(String funcao, DatagramSocket datagramSocket) {
            this.funcao = funcao;
            this.datagramSocket = datagramSocket;
        }

        public PeerThread(String funcao, ServerSocket serverSocket) {
            this.funcao = funcao;
            this.serverSocket = serverSocket;
        }

        public PeerThread(String funcao, String fileName, Socket socket, String ipPort) {
            this.funcao = funcao;
            this.fileName = fileName;
            this.socket = socket;
            this.ipPort = ipPort;
        }

        public PeerThread(String funcao, String fileName) {
            this.funcao = funcao;
            this.fileName = fileName;
        }

        public void run() {
            boolean running = true;
            // If responsável por escutar e retornar o ALIVE que vem do Server
            if (this.funcao.equals("ALIVE")) {
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        datagramSocket.receive(packet);
                        Mensagem retorno = new Mensagem("ALIVE_OK");
                        buf = new Gson().toJson(retorno).getBytes();
                        packet = new DatagramPacket(buf, buf.length, address, packet.getPort());
                        datagramSocket.send(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // If responsável por escutar as portas TCP e fazer o processamento da
                // requisição e enviar o arquivo
            } else if (this.funcao.equals("ESCUTAR-TCP")) {
                try {
                    String receivedData;
                    while (running) {
                        DataOutputStream dataOutputStream = null;
                        DataInputStream dataInputStream = null;
                        Socket clientSocket = serverSocket.accept();
                        dataInputStream = new DataInputStream(clientSocket.getInputStream());
                        dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                        receivedData = dataInputStream.readUTF();
                        Mensagem mensagem = new Gson().fromJson(receivedData, Mensagem.class);
                        if (!fileList.contains(mensagem.getFileName())) {
                            Mensagem retorno = new Mensagem("DOWNLOAD_NEGADO");
                            dataOutputStream.writeUTF(new Gson().toJson(retorno));
                        } else {
                            // Negativa Aletória
                            int random = (int) Math.floor(Math.random() * (9 - 0 + 1) + 0);
                            if (random % 2 == 0) {
                                sendFile(path + "\\" + mensagem.getFileName(), dataOutputStream);
                            } else {
                                Mensagem retorno = new Mensagem("DOWNLOAD_NEGADO");
                                dataOutputStream.writeUTF(new Gson().toJson(retorno));
                            }
                        }
                        dataInputStream.close();
                        dataOutputStream.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // If responsável por enviar a requisição para a porta TCP e verificar o status
                // do arquivo, uma vez que o Download foi bem sucedido, realizar o UPDATE com o
                // Server
            } else if (this.funcao.equals("REQUEST-DOWNLOAD")) {
                DataOutputStream dataOutputStream = null;
                DataInputStream dataInputStream = null;
                try {
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    Mensagem mensagem = new Mensagem("DOWNLOAD", fileName);
                    dataOutputStream.writeUTF(new Gson().toJson(mensagem));
                    boolean isDownloadOk = false;
                    String retorno = "";
                    // Realizar um tentativa de converter o arquivo para json, caso falhe, entende
                    // que o que chegou foi o hash do arquivo
                    try {
                        retorno = dataInputStream.readUTF();
                        Mensagem mensagemRetorno = new Gson().fromJson(retorno, Mensagem.class);
                        if (mensagemRetorno.getAction().equals("DOWNLOAD_NEGADO")) {
                            System.out.println("Peer [" + ipPort + "] negou  o download");
                            isDownloadOk = false;
                        }
                    } catch (Exception e) {
                        isDownloadOk = true;
                    }
                    if (isDownloadOk) {
                        String newFile = path + "\\" + fileName;
                        receiveFile(newFile, dataInputStream);
                        File file = new File(newFile);
                        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                        String hashNewFile = getFileChecksum(md5Digest, file);
                        // Compara o hash enviado pelo Peer Origem pelo o que foi salvo pelo Peer
                        // Destino
                        if (retorno.equals(hashNewFile)) {
                            System.out.println("Arquivo [" + fileName + "] baixado com sucesso na pasta: " + path);
                            fileList.add(fileName);
                            new PeerThread("UPDATE", fileName).start();
                        } else {
                            System.out.println("Peer [" + ipPort + "] negou  o download");
                        }
                    }
                    dataInputStream.close();
                    dataInputStream.close();
                    socket.close();
                } catch (Exception e) {
                    System.out.println("Peer [" + ipPort + "] negou o download");
                    e.printStackTrace();
                }
            } else if (this.funcao.equals("UPDATE")) {
                Mensagem mensagem = new Mensagem("UPDATE", fileName, peerUrl);
                Mensagem mensagemUpdate = sendEcho(mensagem);
            }
        }
    }

    /**
     * Método responsável por receber o arquivo
     * 
     * @param fileName
     * @param dataInputStream
     * @throws Exception
     */
    private static void receiveFile(String fileName, DataInputStream dataInputStream) throws Exception {
        int bytes = 0;
        File file = new File(fileName); // initialize File object and passing path as argument
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = dataInputStream.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        fileOutputStream.close();
    }

    /**
     * Método responsável por enviar o arquivo, ele envia o Hash/Tamanho e próprio
     * arquivo
     * 
     * @param path             - Caminho do arquivo a ser enviado
     * @param dataOutputStream
     * @throws Exception
     */
    private static void sendFile(String path, DataOutputStream dataOutputStream) throws Exception {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file hash
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        String hash = getFileChecksum(md5Digest, file);

        dataOutputStream.writeUTF(hash);

        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }

    /**
     * Método responsável por validar via Hash MD5 se o arquivo foi enviado
     * corretamente
     * 
     * @param digest
     * @param file
     * @return
     * @throws IOException
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        // Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        ;

        // close the stream; We don't need it now.
        fis.close();

        // Get the hash's bytes
        byte[] bytes = digest.digest();

        // This bytes[] has bytes in decimal format;
        // Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        // return complete hash
        return sb.toString();
    }
}