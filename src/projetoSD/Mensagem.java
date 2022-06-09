package projetoSD;

import java.util.*;
public class Mensagem {
    private String fileName;
    private List<String> fileList;
    private String fileFolder;
    private String ip;
    private String port;
    private String content;
    private String action;

    public Mensagem(String action){
        this.action = action;
    }
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileFolder() {
        return this.fileFolder;
    }

    public void setFileFolder(String fileFolder) {
        this.fileFolder = fileFolder;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
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
