package projetoSD;

import java.io.StringReader;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
public class Mensagem {
    private List<String> fileList;
    private String portUdp;
    private List<String> portsTcp;
    private String content;
    private String action;
    public Mensagem(){}
    public Mensagem(byte[] buf){
        String json = new String(buf);
        System.out.println("Message: "+json);
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        Mensagem fromJson = new Gson().fromJson(reader, Mensagem.class);
        this.fileList = fromJson.getFileList();
        this.portUdp = fromJson.getPortUdp();
        this.content = fromJson.getContent();
        this.action = fromJson.getAction();
        this.portsTcp = fromJson.getPortsTcp();
    }

    public Mensagem(List<String> fileList, String portUdp,List<String> portsTcp, String content, String action) {
        this.fileList = fileList;
        this.portUdp = portUdp;
        this.content = content;
        this.action = action;
        this.portsTcp = portsTcp;
    }

    public Mensagem(String action){
        this.action = action;
    }

    public List<String> getPortsTcp() {
        return this.portsTcp;
    }

    public void setPortTcp(List<String> portsTcp) {
        this.portsTcp = portsTcp;
    }

    public String getPortUdp() {
        return this.portUdp;
    }

    public void setPortUdp(String portUdp) {
        this.portUdp = portUdp;
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


}
