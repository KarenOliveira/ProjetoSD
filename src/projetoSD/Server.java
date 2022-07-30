package projetoSD;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;

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
        
        //receive file hash
        String hash = dataInputStream.readUTF();
        System.out.println("Received hash: "+hash);
        
        long size = dataInputStream.readLong();     // read file size
        System.out.println(size);
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);
        
        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0; 
            
        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        
        //close the stream; We don't need it now.
        fis.close();
        
        //Get the hash's bytes
        byte[] bytes = digest.digest();
        
        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        //return complete hash
        return sb.toString();
    }
}
