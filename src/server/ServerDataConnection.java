package server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * FTP传输文件服务器部分
 */

public class ServerDataConnection{

    String IP;
    int port;    // 主动模式下的默认数据传输端口
    Socket socket;
    PrintWriter sendToClient;
    BufferedReader receiveFromClient;

    ServerDataConnection(String IP, int port) throws IOException {
        this.IP = IP;
        this.port = port;
        this.socket = new Socket(InetAddress.getByName(IP), port);
        this.sendToClient = new PrintWriter(socket.getOutputStream(), true);
        this.receiveFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void download(String text) throws IOException {
        String pathname = "";    // 解析下载文件路径
        String[] str = text.split(" ");
        for(int i = 1; i < str.length; ++i){
            pathname += (str[i] + " ");
        }
        FileInputStream lengthCheck = new FileInputStream(pathname);
        int fileLength = 0;
        int content;
        while((content = lengthCheck.read()) != -1){
            ++fileLength;
        }
        sendToClient.println(fileLength);      // 获取文件大小告知客户端
        lengthCheck.close();
        OutputStream os = socket.getOutputStream();   // 获取客户端的输出流
        FileInputStream is = new FileInputStream(pathname);
        while ((content = is.read()) != -1){
            os.write(content);
        }
        is.close();
    }

    public void upload(String text){
        try {
            String[] str = text.split("/");    // 解析以/为分割线的文件路径
            if(str.length == 1){
                str = text.split("\\\\");      // 解析以\\为分割线的文件路径
            }
            String filename = str[str.length - 1];        // 最后一个数据是文件名
            int fileLength = Integer.parseInt(receiveFromClient.readLine());
            InputStream is = socket.getInputStream();  // 获取输入流
            FileOutputStream fos = new FileOutputStream(Server.ftpPath + "/Upload/" + filename);
            for(int i = 0; i < fileLength; ++i){
                fos.write(is.read());   // 将文件上传到服务器
            }
            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
