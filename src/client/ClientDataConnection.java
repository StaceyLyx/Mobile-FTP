package client;

import server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP客户端传输文件类
 * ASCLL传输：txt、html等
 * BINARY传输（default）：exe、图片、视频、音频、压缩文件等
 */

public class ClientDataConnection{

    int port;
    boolean on;
    ServerSocket socket;       // 客户端数据传输端口
    Socket serverSocket;       // 链接服务器的端口
    PrintWriter sendToServer;
    BufferedReader receiveFromServer;
    String type = "ascll";

    ClientDataConnection(int port) throws IOException {
        this.port = port;
        this.on = true;
        this.socket = new ServerSocket(port);
        this.serverSocket = socket.accept();    // 等待客户端来连接
        this.sendToServer = new PrintWriter(serverSocket.getOutputStream(), true);
        this.receiveFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    }

    // 上传文件
    public boolean upload(String pathname){
        if(type.equalsIgnoreCase("ascll")){
            return uploadAscll(pathname);
        }else{
            // 默认二进制传输
            return uploadBinary(pathname);
        }
    }

    // ASCLL传输
    public boolean uploadAscll(String pathname){
        try{
            File file = new File(pathname);
            sendToServer.println("ready");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            PrintWriter printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
            String temp;
            while((temp = bufferedReader.readLine()) != null){
                printWriter.println(temp);
            }
            bufferedReader.close();
            printWriter.close();
            return true;
        }catch (IOException e){
            sendToServer.println("stop");
            return false;
        }
    }

    // 二进制传输
    public boolean uploadBinary(String pathname){
        try{
            BufferedInputStream lengthCheck = new BufferedInputStream(new FileInputStream(pathname));
            sendToServer.println("ready");
            int fileLength = 0;
            int content;
            while(lengthCheck.read() != -1){
                ++fileLength;
            }
            sendToServer.println(fileLength);      // 获取文件大小上传至服务器
            lengthCheck.close();
            BufferedOutputStream os = new BufferedOutputStream(serverSocket.getOutputStream());   // 获取服务器的输出流
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(pathname));
            while ((content = is.read()) != -1){
                os.write(content);
            }
            is.close();
            os.close();
            return true;
        }catch (IOException e){
            sendToServer.println("stop");
            return false;
        }
    }

    // 下载文件
    public boolean download(String pathname) throws IOException {
        if(type.equalsIgnoreCase("ascll")){
            return downloadAscll(pathname);
        }else{
            // 默认二进制传输
            return downloadBinary(pathname);
        }
    }

    // ASCLL传输
    public boolean downloadAscll(String pathname) throws IOException {
        String confirm = receiveFromServer.readLine();
        if(confirm.equals("ready")){
            String[] str = pathname.split("/");    // 解析以/为分割线的文件路径
            if(str.length == 1){
                str = pathname.split("\\\\");      // 解析以\\为分割线的文件路径
            }
            String filename = str[str.length - 1];        // 最后一个数据是文件名
            File file = new File("Client Files/Download/" + filename);
            PrintWriter printWriter = new PrintWriter(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String temp;
            while((temp = bufferedReader.readLine()) != null){
                printWriter.println(temp);
            }
            printWriter.close();
            bufferedReader.close();
            return true;
        }else{
            return false;
        }
    }

    // 二进制传输
    public boolean downloadBinary(String pathname) throws IOException{
        String confirm = receiveFromServer.readLine();
        if(confirm.equals("ready")){
            String[] str = pathname.split("/");    // 解析以/为分割线的文件路径
            if(str.length == 1){
                str = pathname.split("\\\\");      // 解析以\\为分割线的文件路径
            }
            String filename = str[str.length - 1];        // 最后一个数据是文件名
            int fileLength = Integer.parseInt(receiveFromServer.readLine());
            BufferedInputStream is = new BufferedInputStream(serverSocket.getInputStream());   // 获取输入流
            FileOutputStream fos = new FileOutputStream("Client Files/Download/" + filename);
            BufferedOutputStream os = new BufferedOutputStream(fos);
            for(int i = 0; i < fileLength; ++i){
                os.write(is.read());   // 将文件下载到客户端
            }
            is.close();
            fos.close();
            return true;
        }else{
            return false;
        }
    }

    public void close() throws IOException {
        this.type = "";
        this.on = false;
        socket.close();
        serverSocket.close();
    }

}
