package projetoSD;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(5000);
            while(true){
                System.out.println("listening to port:5000");
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket+" connected.");
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                System.out.println(dataInputStream.readUTF());
                receiveFile("C:\\Users\\karen\\Documents\\MCTB001 - NAB2SA - 1q22 - Aula 2.mp4");
                //receiveFile("NewFile2.pdf");
                System.out.println("File received.");
                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                serverSocket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        File file = new File(fileName); //initialize File object and passing path as argument  
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        long size = dataInputStream.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }
}
