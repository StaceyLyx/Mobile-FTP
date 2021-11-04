package server;

import java.io.*;
import java.net.Socket;

public class ConnectClient implements Runnable{

    Socket clientSocket;     // FTP客户端套接字
    String ftpPath;
    BufferedReader receiveFromClient;    // 接受客户端的信息输入流
    PrintWriter sentToClient;     // 服务端发送给客户端的信息输出流

    ConnectClient(Socket clientSocket, String ftpPath){
        this.clientSocket = clientSocket;
        this.ftpPath = ftpPath;
    }

    @Override
    public void run() {
        try{
            // 与客户端的输入输出流连接
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            receiveFromClient = new BufferedReader(new InputStreamReader(is));
            sentToClient = new PrintWriter(os, true);

            sentToClient.println("FTP login successfully!");
            while(true){
                sentToClient.print("Please enter command > : ");
                String text = receiveFromClient.readLine();
                System.out.println("command: " + text);
                if(text.equalsIgnoreCase("quit")){
                    System.out.println("Client logout!");
                    sentToClient.println("FTP logout successfully!");
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
