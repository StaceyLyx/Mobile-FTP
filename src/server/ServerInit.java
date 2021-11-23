package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP服务器
 */

public class ServerInit {

    static String ftpPath = "./Share";
    static Authority serverAuthority = new Authority();

    // 初始化FTP服务器
    public void init(){      // 服务器创建通信ServerSocket，创建Socket接收客户端信息
        File uploadDirectory = new File(ftpPath + "/Upload");
        if(!uploadDirectory.exists()){
            uploadDirectory.isDirectory();
            uploadDirectory.mkdir();
        }
        File downloadDirectory = new File(ftpPath + "/Download");
        if(!downloadDirectory.exists()){
            downloadDirectory.isDirectory();
            downloadDirectory.mkdir();
        }
    }

    // 运行FTP服务器
    public void run(Server server){
        while(true){
            // 等待接收多个客户端链接
            try{
                server.clientSocket = server.serverSocket.accept();
                Thread thread = new Thread(server);
                thread.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
