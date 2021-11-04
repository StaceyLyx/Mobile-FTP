package client;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 运行FTP客户端主程序
 */

public class RunClient {

    public static void main(String[] args){
        Client client = new Client();     // 生成客户端
        String localhostAddress = "";
        try{
            localhostAddress = String.valueOf(InetAddress.getLocalHost());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        Socket clientSocket = client.init(localhostAddress.split("/")[1], 6500);    // 生成客户端套接字
        try{
            client.run(clientSocket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
