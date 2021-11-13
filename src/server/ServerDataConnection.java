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
    // TODO：acsll还是binary

    ServerDataConnection(String IP, int port) throws IOException {
        this.IP = IP;
        this.port = port;
        this.socket = new Socket(InetAddress.getByName(IP), port);
        this.sendToClient = new PrintWriter(socket.getOutputStream(), true);
        this.receiveFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public boolean download(String text){
        try{
            StringBuilder pathname = new StringBuilder();    // 解析下载文件路径
            String[] str = text.split(" ");
            if(str.length == 1) throw new IndexOutOfBoundsException();
            for(int i = 1; i < str.length; ++i){
                pathname.append(str[i]).append(" ");
            }
            BufferedInputStream lengthCheck = new BufferedInputStream(new FileInputStream(pathname.toString()));
            sendToClient.println("ready");
            System.out.println("ready to download file ......");
            int fileLength = 0;
            int content;
            while(lengthCheck.read() != -1){
                ++fileLength;
            }
            sendToClient.println(fileLength);      // 获取文件大小告知客户端
            lengthCheck.close();
            BufferedOutputStream os = new BufferedOutputStream(socket.getOutputStream());    // 获取客户端的输出流
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(pathname.toString()));
            while ((content = is.read()) != -1){
                os.write(content);
            }
            is.close();
            os.close();
            return true;
        }catch (IOException e){
            sendToClient.println("stop");
            return false;
        }
    }

    public boolean upload(String text) throws IOException{
        String confirm = receiveFromClient.readLine();
        if(confirm.equals("ready")){
            String[] str = text.split("/");    // 解析以/为分割线的文件路径
            if(str.length == 1){
                str = text.split("\\\\");      // 解析以\\为分割线的文件路径
            }
            if(str.length == 1) throw new IndexOutOfBoundsException();
            System.out.println("ready to upload file ......");
            String filename = str[str.length - 1];        // 最后一个数据是文件名
            int fileLength = Integer.parseInt(receiveFromClient.readLine());
            BufferedInputStream is = new BufferedInputStream(socket.getInputStream());    // 获取输入流
            FileOutputStream fos = new FileOutputStream(Server.ftpPath + "/Upload/" + filename);
            BufferedOutputStream os = new BufferedOutputStream(fos);
            for(int i = 0; i < fileLength; ++i){
                os.write(is.read());   // 将文件上传到服务器
            }
            is.close();
            fos.close();
            return true;
        }else{
            return false;
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
