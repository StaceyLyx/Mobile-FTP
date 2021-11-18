package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP传输文件服务器部分
 */

public class ServerDataConnection{

    boolean on;
    String IP;
    int port;       // 数据传输端口
    Socket socket;
    String type = "";
    BufferedInputStream is;       // 客户端输入流：binary
    BufferedOutputStream os ;    // 客户端输出流：binary
    PrintWriter printWriter;     // 客户端输出流：ascii
    BufferedReader bufferedReader;    // 客户端输入流：ascii

    // 主动模式
    ServerDataConnection(String IP, int port) throws IOException {
        this.on = true;
        this.IP = IP;
        this.port = port;
        this.socket = new Socket(InetAddress.getByName(IP), port);
        this.is = new BufferedInputStream(socket.getInputStream());
        this.os = new BufferedOutputStream(socket.getOutputStream());
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // 被动模式
    ServerDataConnection(int port) throws IOException {
        this.on = true;
        this.port = port;
        ServerSocket serverSocket = new ServerSocket(this.port);
        this.socket = serverSocket.accept();
        this.is = new BufferedInputStream(socket.getInputStream());
        this.os = new BufferedOutputStream(socket.getOutputStream());
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void setType(String type){
        this.type = type;
    }

    // 上传文件
    public boolean upload(BufferedReader receiveFromClient) throws IOException {
        String info = receiveFromClient.readLine();
        if(info.equals("ready")){
            // 传输特定文件
            String filename = receiveFromClient.readLine();         // 收取文件名
            System.out.println("ready to upload \"" + filename + "\" ......");
            if (type.equalsIgnoreCase("ascii")) {
                uploadAscii(Server.ftpPath + "/Upload/" + filename);
            }else{
                // 默认二进制传输
                uploadBinary(Server.ftpPath + "/Upload/" + filename, receiveFromClient);
            }
            return true;
        }else if(info.equals("stop")){
            // 异常停止
            return false;
        }else{
            // 传输文件夹及内部文件
            String path = Server.ftpPath + "/Upload/" + info + "/";
            // 新建文件夹
            File file = new File(Server.ftpPath + "/Upload/" + info);
            if (!file.exists()) {
                file.isDirectory();
                file.mkdir();
            }
            while(!(info = receiveFromClient.readLine()).equals("over")){
                if(info.equals("ready")){
                    // 传输特定文件
                    String filename = receiveFromClient.readLine();
                    System.out.println("ready to upload \"" + filename + "\" ......");
                    if (type.equalsIgnoreCase("ascii")) {
                        uploadAscii(path + filename);
                    }else{
                        // 默认二进制传输
                        uploadBinary(path + filename, receiveFromClient);
                    }
                }else if(info.equals("stop")){
                    // 异常停止
                    return false;
                }
            }
            System.out.println("all uploading requests are done");
        }
        return true;
    }

    // ASCLL传输
    public void uploadAscii(String filepath) throws IOException {
        File file = new File(filepath);
        PrintWriter filePrintWriter = new PrintWriter(file);

        String temp;
        while((temp = bufferedReader.readLine()) != null){
            filePrintWriter.println(temp);
        }
        System.out.println("one file has uploaded");
        filePrintWriter.close();
    }

    // 二进制传输
    public void uploadBinary(String filepath, BufferedReader receiveFromClient) throws IOException{
        int fileLength = Integer.parseInt(receiveFromClient.readLine());
        FileOutputStream fos = new FileOutputStream(filepath);
        BufferedOutputStream fileOs = new BufferedOutputStream(fos);
        for(int i = 0; i < fileLength; ++i){
            fileOs.write(is.read());   // 将文件上传到服务器
        }
        fileOs.flush();
        System.out.println("one file has uploaded");
        fileOs.close();
    }

    // 下载文件
    public boolean download(String text, PrintWriter sendToClient) throws IOException {
        StringBuilder pathname = new StringBuilder();    // 解析下载文件路径
        String[] str = text.split(" ");
        if(str.length == 1) throw new IndexOutOfBoundsException();
        for(int i = 1; i < str.length; ++i){
            pathname.append(str[i]);
            if(i != str.length - 1){
                pathname.append(" ");
            }
        }
        File file = new File(pathname.toString());
        if(!file.exists()){
            sendToClient.println("stop");
            return false;
        }
        if(file.isDirectory()){
            // 传输文件夹
            sendToClient.println(file.getName());   // 新建文件夹
            File[] files = file.listFiles();
            assert files != null;
            // 传输file文件夹中的所有文件
            for (File temp : files) {
                if(type.equalsIgnoreCase("ascll")) {
                    if(!downloadAscii(temp.getPath(), sendToClient)){
                        return false;
                    }
                }else{
                    // 默认二进制传输
                    if(!downloadBinary(temp.getPath(), sendToClient)){
                        return false;
                    }
                }
            }
            System.out.println("all downloading requests are done");
            sendToClient.println("over");
            return true;
        }else{
            // 传输特定文件
            if(type.equalsIgnoreCase("ascll")){
                return downloadAscii(pathname.toString(), sendToClient);
            }else{
                // 默认二进制传输
                return downloadBinary(pathname.toString(), sendToClient);
            }
        }
    }

    public boolean downloadAscii(String pathname, PrintWriter sendToClient){
        try{
            BufferedReader fileBufferedReader = new BufferedReader(new FileReader(pathname));
            sendToClient.println("ready");
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
            sendToClient.println("stop");
            return false;
        }
    }

    public boolean downloadBinary(String pathname, PrintWriter sendToClient){
        try{
            BufferedInputStream lengthCheck = new BufferedInputStream(new FileInputStream(pathname));
            sendToClient.println("ready");
            File file = new File(pathname);
            System.out.println("ready to download \"" + file.getName() + "\" ......");
            sendToClient.println(file.getName());
            int fileLength = 0;
            int content;
            while(lengthCheck.read() != -1){
                ++fileLength;
            }
            sendToClient.println(fileLength);      // 获取文件大小告知客户端
            lengthCheck.close();
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
            sendToClient.println("stop");
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
