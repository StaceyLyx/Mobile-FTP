package server;

import java.io.File;
import java.io.IOException;

/**
 * FTP服务器
 */

public class ServerInit {

    int port;
    static String ftpPath = "./Share";
    static Authority serverAuthority = new Authority();
    boolean finishOne = false;

    // 初始化FTP服务器
    public void init(int port){      // 服务器创建通信ServerSocket，创建Socket接收客户端信息
        this.port = port;
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
    public void run(){
        while(true){
            // 等待接收多个客户端链接
            if(!finishOne){
                try{
                    Thread thread = new Thread(new Server(ServerInit.ftpPath, this.port));
                    thread.start();
                    finishOne = true;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
