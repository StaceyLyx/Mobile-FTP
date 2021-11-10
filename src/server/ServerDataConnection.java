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

    ServerDataConnection(String IP, int port) throws IOException {
        this.IP = IP;
        this.port = port;
        this.socket = new Socket(InetAddress.getByName(IP), port);
    }

    public void downloadFile(String pathname){

    }

    public void uploadFile(){
//        Socket socket;
//        try {
//
//            InputStream is = socket.getInputStream();  // 获取输入流
//            OutputStream os = socket.getOutputStream();   // 输出流
//            FileOutputStream fos = new FileOutputStream(pathname);
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = is.read(buffer)) != -1){
//                fos.write(buffer, 0, len);
//            }
//            os.write("done".getBytes(StandardCharsets.UTF_8));
//            os.close();
//            fos.close();
//            is.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
