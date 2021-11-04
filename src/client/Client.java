package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * FTP客户端
 */

public class Client {

    BufferedReader receiveFromServer;    // 接受服务器的信息输入流
    PrintWriter sentToServer;     // 客户端发送给服务器的信息输出流

    // 初始化FTP客户端
    public Socket init(String ip, int port){      // 客户端创建Socket通信，设置通信服务器的ip与port
        Socket socket = null;
        try{
            socket = new Socket(InetAddress.getByName(ip), port);
            File downloadDirectory = new File("Download");
            if(!downloadDirectory.exists()){
                downloadDirectory.isDirectory();
                downloadDirectory.mkdir();
            }
        }catch (IOException e){
            System.out.println("error in FTP client setting");
            e.printStackTrace();
        }
        return socket;
    }

    // 运行FTP客户端
    public void run(Socket clientSocket){

        // 与服务器交互的输入输出流
        try{
            String instruction = "";
            BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            sentToServer = new PrintWriter(os, true);     // 发送给服务器的信息流
            receiveFromServer = new BufferedReader(new InputStreamReader(is));    // 接收服务器的信息流
            System.out.println(receiveFromServer.readLine());

            // 进入用户操作
            while(true){
                instruction = keyboardIn.readLine();
                if(instruction.equalsIgnoreCase("quit")){
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
