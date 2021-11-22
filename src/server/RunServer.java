package server;

import java.net.ServerSocket;

/**
 * 运行FTP服务器主程序
 */

// 服务器:ServerSocket
// 客户端:Socket

public class RunServer {

    public static void main(String[] args){
        ServerInit server = new ServerInit();   // 生成服务器
        server.init();
        try{
            server.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
