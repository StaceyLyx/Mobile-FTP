package server;

import java.io.*;
import java.net.Socket;

/**
 * FTP服务器链接客户端
 */

public class ConnectClient implements Runnable{

    Socket clientSocket;     // FTP客户端套接字
    String ftpPath;
    BufferedReader receiveFromClient;    // 接受客户端的信息输入流
    PrintWriter sendToClient;     // 服务端发送给客户端的信息输出流

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
            sendToClient = new PrintWriter(os, true);

            sendToClient.println("FTP login successfully!");
            sendToClient.println("Please enter commands.");
            ServerDataConnection dataConnection = null;    // 用于等待客户端的数据链接
            while(true){
                String text = receiveFromClient.readLine();
                System.out.println("command: " + text);
                if(text.equals("quit")){
                    System.out.println("Client logout!");
                    sendToClient.println("FTP logout successfully!");
                    break;
                }else if(text.equals("port")){
                    // 等待接收待连接的IP地址与端口号，服务器主动建立数据连接
                    String IP = receiveFromClient.readLine();
                    int port = Integer.parseInt(receiveFromClient.readLine());
                    try{
                        dataConnection = new ServerDataConnection(IP, port);
                    }catch (IOException e){
                        sendToClient.println("Data connection failed");
                        System.out.println("Data connection failed");
                        continue;
                    }
                    System.out.println("Data connection is finished, client port is " + port);
                }else if(text.startsWith("pasv") || text.startsWith("PASV")){

                }else if(text.startsWith("retr") || text.startsWith("RETR")){
                    // 下载文件到客户端
                    if(dataConnection != null){
                        try{
                            System.out.println("ready to download file ......");
                            dataConnection.download(text);
                            System.out.println(receiveFromClient.readLine());
                            dataConnection.close();    // 关闭数据链接
                        }catch (IOException e){
                            System.out.println("error occurs");
                        }
                    }else{
                        System.out.println("nonexistent connection");
                        sendToClient.println("There is no data connection");
                    }
                }else if(text.startsWith("stor") || text.startsWith("STOR")){
                    // 上传文件
                    if(dataConnection != null){
                        System.out.println("ready to upload file ......");
                        dataConnection.upload(text);
                        System.out.println(receiveFromClient.readLine());
                        dataConnection.close();
                    }else{
                        System.out.println("nonexistent connection");
                        sendToClient.println("There is no data connection");
                    }
                }else{
                    System.out.println("unknown command");
                    continue;
                }
                sendToClient.println("command \"" + text + "\" is done.");
            }
            receiveFromClient.close();
            sendToClient.close();
            clientSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
