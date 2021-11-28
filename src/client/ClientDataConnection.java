package client;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * FTP客户端传输文件类
 * ASCLL传输：txt、html等
 * BINARY传输（default）：exe、图片、视频、音频、压缩文件等
 */

public class ClientDataConnection{

    // 数据传输设置相关
    String ip;
    int port;
    boolean on;
    String type = "";
    ServerSocket socket;       // 客户端数据传输端口
    Socket serverSocket;       // 链接服务器的端口

    BufferedInputStream is = null;   // 服务器输入流:binary
    BufferedOutputStream os = null;        // 服务器输出流:binary
    BufferedReader bufferedReader = null;   // 服务器输入流:ascii
    PrintWriter printWriter = null;  // 服务器输出流:ascii

    // 主动模式
    ClientDataConnection(int port) throws IOException {
        this.port = port;
        this.on = true;
        this.socket = new ServerSocket(port);
        this.serverSocket = socket.accept();    // 等待客户端来连接
        if(this.type.equalsIgnoreCase("ascii")){
            this.bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
        }else{
            this.is = new BufferedInputStream(serverSocket.getInputStream());
            this.os = new BufferedOutputStream(serverSocket.getOutputStream());
        }
    }

    // 被动模式
    ClientDataConnection(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.on = true;
        this.serverSocket = new Socket(InetAddress.getByName(ip), port);
        if (this.type.equalsIgnoreCase("ascii")) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
        }else{
            this.is = new BufferedInputStream(serverSocket.getInputStream());
            this.os = new BufferedOutputStream(serverSocket.getOutputStream());
        }
    }

    public void setType(String type) throws IOException {
        this.type = type;
        if (this.type.equalsIgnoreCase("ascii")) {
            this.bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            this.printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
        }else{
            this.is = new BufferedInputStream(serverSocket.getInputStream());
            this.os = new BufferedOutputStream(serverSocket.getOutputStream());
        }
    }

    // 上传文件
    public boolean uploadDirectory(File[] files){
        // 传输file文件夹中的所有文件
        for (File file : files) {
            if(!uploadFile(file)){
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

    public boolean uploadFile(File file){
        if(type.equalsIgnoreCase("ascii")) {
            return uploadAscii(file.getPath());
        }else{
            // 默认二进制传输
            return uploadBinary(file.getPath());
        }
    }

    // ASCII传输
    public boolean uploadAscii(String pathname){
        try{
            File file = new File(pathname);
            printWriter.println(file.getName());
            BufferedReader fileBufferedReader = new BufferedReader(new FileReader(file));
            String temp;
            while((temp = fileBufferedReader.readLine()) != null){
                printWriter.println(temp);
            }
            fileBufferedReader.close();
            return true;
        }catch (IOException e){
            printWriter.println("stop");
            return false;
        }
    }

    // 二进制传输
    public boolean uploadBinary(String pathname){
        try{
            // 传输文件名
            os.write(new File(pathname).getName().getBytes(StandardCharsets.UTF_8));
            os.flush();
            // 传输文件大小
            byte[] bytes = new byte[1024];
            int length = is.read(bytes);
            String check = new String(bytes, 0, length);
            if(check.equals("continue")){
                BufferedInputStream lengthCheck = new BufferedInputStream(new FileInputStream(pathname));
                int fileLength = 0;
                while (lengthCheck.read() != -1){
                    ++fileLength;
                }
                os.write(Integer.toString(fileLength).getBytes(StandardCharsets.UTF_8));
                os.flush();
                lengthCheck.close();
            }
            // 传输文件内容
            bytes = new byte[1024];
            length = is.read(bytes);
            check = new String(bytes, 0, length);
            if(check.equals("continue")){
                BufferedInputStream fileIs = new BufferedInputStream(new FileInputStream(pathname));
                int content;
                while ((content = fileIs.read()) != -1){
                    os.write(content);
                }
                os.flush();
                fileIs.close();
            }
            bytes = new byte[1024];
            length = is.read(bytes);
            check = new String(bytes, 0, length);
            return check.equals("finish");
        }catch (IOException e){
            try{
                os.write("stop".getBytes(StandardCharsets.UTF_8));
            }catch (IOException e1){
                return false;
            }
            return false;
        }
    }

    // 下载文件夹及内部文件
    public boolean downloadDirectory(String path) throws IOException {
        while(true) {
            String info;
            if (bufferedReader != null) {
                info = bufferedReader.readLine();
            } else {
                byte[] contents = new byte[1024];
                int length = is.read(contents);
                info = new String(contents, 0, length);
            }
            if (info.equals("stop")) {
                // 异常停止
                return false;
            } else if (info.equals("over")) {
                // 传输结束
                break;
            } else {
                // 传输内部文件
                downloadFile(path + info);
            }
        }
        return true;
    }

    // 下载特定文件
    public boolean downloadFile(String filePath) throws IOException{
        if (type.equalsIgnoreCase("ascii")){
            String filename = bufferedReader.readLine();
            System.out.println("ready to upload \"" + filename + "\" ......");
            downloadAscii(filePath + filename);
        }else{
            // 默认二进制传输
            System.out.println("ready to download file");
            byte[] bytes = new byte[1024];
            int length = is.read(bytes);
            String filename = new String(bytes, 0, length);
            System.out.println("ready to download \"" + filename + "\" ......");
            downloadBinary(filePath + filename);
        }
        return true;
    }

    // ASCII传输
    public void downloadAscii(String filepath) throws IOException {
        File file = new File(filepath);
        PrintWriter filePrintWriter = new PrintWriter(file);
        String temp;
        while((temp = bufferedReader.readLine()) != null){
            filePrintWriter.println(temp);
        }
        filePrintWriter.flush();
        filePrintWriter.close();
    }

    // 二进制传输
    public void downloadBinary(String filepath) throws IOException{
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
    }

    public void close() throws IOException{
        this.type = "";
        this.on = false;
        if(this.socket != null){
            this.socket.close();
        }
        if(this.serverSocket != null) {
            this.serverSocket.close();
        }
    }
}
