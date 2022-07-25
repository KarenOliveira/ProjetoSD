package projetoSD;

import java.io.StringReader;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
public class Mensagem {
    private List<String> fileList;
    private String content;
    private String action;
    private String fileName;
    private List<String> peerList;
    private String peerUrl;


    public String getPeerUrl() {
        return this.peerUrl;
    }

    public void setPeerUrl(String peerUrl) {
        this.peerUrl = peerUrl;
    }
    
    public List<String> getPeerList() {
        return peerList;
    }
    public void setPeerList(List<String> peerList) {
        this.peerList = peerList;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public Mensagem(){}
    public Mensagem(byte[] buf){
        String json = new String(buf);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        Mensagem fromJson = new Gson().fromJson(reader, Mensagem.class);
        this.fileList = fromJson.getFileList();
        this.content = fromJson.getContent();
        this.action = fromJson.getAction();
        this.fileName = fromJson.getFileName();
        this.peerUrl = fromJson.getPeerUrl();
    }
    public Mensagem(List<String> peerList){
        this.peerList = peerList;
    }
    public Mensagem(List<String> fileList, String content, String action) {
        this.fileList = fileList;
        this.content = content;
        this.action = action;
    }

    public Mensagem(String action){
        this.action = action;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getFileList() {
        return this.fileList;
    }

    public void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }
    public static String buildUrl(String ip, String portUdp, List<String> portTcp){
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append(";");
        sb.append(portUdp);
        sb.append(";");
        sb.append(String.join(":", portTcp));
        return sb.toString();
    }
    public static String buildUrl(String ip, int portUdp, List<String> portTcp){
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append(";");
        sb.append(portUdp);
        sb.append(";");
        sb.append(String.join(":", portTcp));
        return sb.toString();
    }
    public String buildUrl(String ip, Mensagem mensagem){
        StringBuilder sb = new StringBuilder();
        String[] split = mensagem.getPeerUrl().split(";");
        sb.append(ip);
        sb.append(";");
        sb.append(split[1]);
        sb.append(";");
        for(int i = 2; i < split.length; i++){
            sb.append(split[i]);
            if(i != split.length - 1)
                sb.append(":");
        }
        return sb.toString();
    }

}
