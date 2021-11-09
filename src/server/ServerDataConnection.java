package server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * FTP传输文件服务器部分
 */

public class ServerFileTransfer implements Runnable{

    String action;
    int port = 7500;    // 主动模式下的默认数据传输端口
    String pathname;
    ServerSocket serverSocket = new ServerSocket(port);

    ServerFileTransfer(String action, String pathname, int port) throws IOException {
        this.action = action;
        this.pathname = pathname;
        this.port = port;
    }

    @Override
    public void run() {
        if(action.equals("download")){
            downloadFile();
        }else if(action.equals("upload")){
            uploadFile();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(){

    }

    public void uploadFile(){
        Socket socket;
        try {
            socket = serverSocket.accept();  // 监听链接
            InputStream is = socket.getInputStream();  // 获取输入流
            OutputStream os = socket.getOutputStream();   // 输出流
            FileOutputStream fos = new FileOutputStream(pathname);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            os.write("done".getBytes(StandardCharsets.UTF_8));
            os.close();
            fos.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
