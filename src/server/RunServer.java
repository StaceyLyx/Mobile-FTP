package server;

import java.net.ServerSocket;

/**
 * 运行FTP服务器主程序
 */

// 服务器:ServerSocket
// 客户端:Socket

public class RunServer {

    public static void main(String[] args){
        ServerInit serverInit = new ServerInit();   // 生成服务器
        serverInit.init(6500);
        try{
            serverInit.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
