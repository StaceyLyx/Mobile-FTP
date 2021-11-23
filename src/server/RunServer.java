package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 运行FTP服务器主程序
 */

// 服务器:ServerSocket
// 客户端:Socket

public class RunServer {

    public static void main(String[] args){
        ServerInit serverInit = new ServerInit();   // 生成服务器
        serverInit.init();
        Server server = new Server();
        server.init();
        try{
            serverInit.run(server);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
