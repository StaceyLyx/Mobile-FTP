package server;

import java.net.ServerSocket;

/**
 * 运行FTP服务器主程序
 */

// 服务器:ServerSocket
// 客户端:Socket

public class RunServer {

    public static void main(String[] args){
        Server server = new Server();   // 生成服务器
        ServerSocket serverSocket = server.init();    // 生成服务器套接字
        try{
            server.run(serverSocket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
