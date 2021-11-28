package server;

import java.io.File;
import java.io.IOException;

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

    // 运行FTP服务器进行客户端监听
    public void run(Server server){
        try{
            server.clientSocket = server.serverSocket.accept();
            Thread thread = new Thread(server);
            thread.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
