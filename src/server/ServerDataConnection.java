package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

/**
 * FTP传输文件服务器部分
 */

public class ServerDataConnection{

    boolean on;
    String ip;
    int port;       // 数据传输端口
    Socket socket;
    String type = "";

    BufferedInputStream is = null;       // 客户端输入流：binary
    BufferedOutputStream os = null;    // 客户端输出流：binary
    PrintWriter printWriter = null;     // 客户端输出流：ascii
    BufferedReader bufferedReader = null;    // 客户端输入流：ascii

    // 主动模式
    ServerDataConnection(String ip, int port) throws IOException {
        this.on = true;
        this.ip = ip;
        this.port = port;
        this.socket = new Socket(InetAddress.getByName(ip), port);
        if(this.type.equalsIgnoreCase("ascii")){
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }else{
            this.is = new BufferedInputStream(socket.getInputStream());
            this.os = new BufferedOutputStream(socket.getOutputStream());
        }
    }

    // 被动模式
    ServerDataConnection(int port) throws IOException {
        this.on = true;
        this.port = port;
        ServerSocket serverSocket = new ServerSocket(this.port);
        this.socket = serverSocket.accept();
        if(this.type.equalsIgnoreCase("ascii")){
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }else{
            this.is = new BufferedInputStream(socket.getInputStream());
            this.os = new BufferedOutputStream(socket.getOutputStream());
        }
    }

    public void setType(String type){
        this.type = type;
    }

    // 上传文件夹
    public boolean uploadDirectory(String path) throws IOException{
        while(true){
            String info;
            if(bufferedReader != null){
                info = bufferedReader.readLine();
            }else{
                byte[] contents = new byte[1024];
                int length = is.read(contents);
                info = new String(contents, 0, length);
            }
            if(info.equals("stop")){
                // 异常停止
                return false;
            }else if(info.equals("over")){
                // 传输结束
                break;
            }else{
                // 传输内部文件
                System.out.println("ready to upload \"" + info + "\" ......");
                if (type.equalsIgnoreCase("ascii")) {
                    uploadAscii(path + info);
                }else{
                    // 默认二进制传输
                    uploadBinary(path + info);
                }
            }
        }
        System.out.println("all uploading requests are done");
        return true;
    }

    // 上传文件特定文件
    public void uploadFile() throws IOException, InterruptedException {
        if (type.equalsIgnoreCase("ascii")){
            String filename = bufferedReader.readLine();
            System.out.println("ready to upload \"" + filename + "\" ......");
            uploadAscii(ServerInit.ftpPath + "/Upload/" + filename);
        }else{
            // 默认二进制传输
            byte[] bytes = new byte[1024];
            int length = is.read(bytes);
            String filename = new String(bytes, 0, length);
            System.out.println("ready to upload \"" + filename + "\" ......");
            uploadBinary(ServerInit.ftpPath + "/Upload/" + filename);
        }
    }

    // ASCII传输
    public void uploadAscii(String filepath) throws IOException {
        File file = new File(filepath);
        PrintWriter filePrintWriter = new PrintWriter(file);
        String temp;
        while((temp = bufferedReader.readLine()) != null){
            filePrintWriter.println(temp);
        }
        filePrintWriter.flush();
        System.out.println("one file has uploaded");
        filePrintWriter.close();
    }

    // 二进制传输
    public void uploadBinary(String filepath) throws IOException{
        // 接收文件大小
        os.write("continue".getBytes(StandardCharsets.UTF_8));
        os.flush();
        byte[] contents = new byte[1024];
        int length = is.read(contents);
        int fileLength = Integer.parseInt(new String(contents, 0, length));
        // 接收文件内容
        os.write("continue".getBytes(StandardCharsets.UTF_8));
        os.flush();
        FileOutputStream fos = new FileOutputStream(filepath);
        BufferedOutputStream fileOs = new BufferedOutputStream(fos);
        for(int i = 0; i < fileLength; ++i){
            fileOs.write(is.read());
        }
        fileOs.flush();
        fileOs.close();
        os.write("finish".getBytes(StandardCharsets.UTF_8));
        os.flush();
        System.out.println("one file has uploaded");
    }

    // 下载文件夹及内部文件
    public boolean downloadDirectory(File[] files){
        // 传输file文件夹中的所有文件
        for (File file : files) {
            if(!downloadFile(file)){
                return false;
            }
        }
        try{
            if(printWriter != null){
                printWriter.println("over");
            }else{
                os.write("over".getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            return true;
        }catch (IOException e){
            return false;
        }
    }

    // 下载文件单独文件
    public boolean downloadFile(File file){
        if(type.equalsIgnoreCase("ascii")) {
            return downloadAscii(file.getPath());
        }else{
            // 默认二进制传输
            return downloadBinary(file.getPath());
        }
    }

    // ASCII下载
    public boolean downloadAscii(String pathname){
        try{
            BufferedReader fileBufferedReader = new BufferedReader(new FileReader(pathname));
            printWriter.println("ready");
            File file = new File(pathname);
            System.out.println("ready to download \"" + file.getName() + "\" ......");
            String temp;
            while((temp = fileBufferedReader.readLine()) != null){
                printWriter.println(temp);
            }
            fileBufferedReader.close();
            System.out.println("one file has downloaded");
            return true;
        }catch (IOException e){
            printWriter.println("stop");
            return false;
        }
    }

    // 二进制下载
    public boolean downloadBinary(String pathname){
        try{
            printWriter.println("ready");
            File file = new File(pathname);
            System.out.println("ready to download \"" + file.getName() + "\" ......");
            printWriter.println(file.getName());
            int content;
            BufferedInputStream fileIs = new BufferedInputStream(new FileInputStream(pathname));
            while ((content = fileIs.read()) != -1){
                os.write(content);
            }
            os.flush();
            System.out.println("one file has downloaded");
            fileIs.close();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            printWriter.println("stop");
            return false;
        }
    }

    public void close() throws IOException {
        this.on = false;
        if(this.socket != null){
            this.socket.close();
        }
    }
}
