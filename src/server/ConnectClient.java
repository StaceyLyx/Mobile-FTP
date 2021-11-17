package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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

            ServerDataConnection dataConnection = null;    // 用于等待客户端的数据链接
            while(true){
                String text = receiveFromClient.readLine();
                System.out.println("command: " + text);
                if(text.equals("quit")){
                    System.out.println("Client logout!");
                    sendToClient.println("FTP logout successfully!");
                    break;
                }else if(text.equals("login")){
                    String user = receiveFromClient.readLine();
                    String pass = receiveFromClient.readLine();
                    if(Server.serverAuthority.authorityLegal(user, pass)){
                        sendToClient.println("legal");
                        System.out.println("Client Login!");
                    }else{
                        sendToClient.println("illegal");
                        System.out.println("illegal user");
                    }
                }else if(text.equals("port")){
                    // 等待接收待连接的IP地址与端口号，服务器主动建立数据连接
                    String IP = receiveFromClient.readLine();
                    int port = Integer.parseInt(receiveFromClient.readLine());
                    try{
                        dataConnection = new ServerDataConnection(IP, port);
                        sendToClient.println("command \"" + text + "\" is done.");
                    }catch (IOException e){
                        sendToClient.println("Data connection failed");
                        System.out.println("Data connection failed");
                        continue;
                    }
                    System.out.println("Data connection is finished, client port is " + port);
                }else if(text.startsWith("type") || text.startsWith("TYPE")){
                    if(dataConnection!=null){
                        String type = receiveFromClient.readLine();
                        dataConnection.setType(type);
                        sendToClient.println("current data transmission type: " + type);
                        System.out.println("get type successfully!");
                        System.out.println("current type: " + type);
                    }else{
                        System.out.println("Data connection failed");
                    }
                }else if(text.startsWith("pasv") || text.startsWith("PASV")){

                }else if(text.startsWith("retr") || text.startsWith("RETR")){
                    // 下载文件到客户端
                    if(dataConnection != null && dataConnection.on){
                        try{
                            sendToClient.println("accept");
                            if(dataConnection.download(text, sendToClient)){
                                dataConnection.close();    // 关闭数据链接
                                String check = receiveFromClient.readLine();
                                if(check.equals("continue")){
                                    sendToClient.println("command \"" + text + "\" is done.");
                                }
                            }else{
                                receiveFromClient.readLine();
                                System.out.println("errors occur");
                            }
                        }catch (IndexOutOfBoundsException e){
                            System.out.println("command needs parameter");
                        }catch (IOException e){
                            System.out.println("errors occur");
                        }
                    }else{
                        System.out.println("nonexistent data connection");
                    }
                }else if(text.startsWith("stor") || text.startsWith("STOR")){
                    // 上传文件
                    if(dataConnection != null && dataConnection.on){
                        try{
                            if(dataConnection.upload(receiveFromClient)){
                                dataConnection.close();    // 关闭数据链接
                                sendToClient.println("command \"" + text + "\" is done.");
                            }else{
                                System.out.println("client error");
                            }
                        }catch (IndexOutOfBoundsException e){
                            System.out.println("command needs parameter");
                        }catch (IOException e){
                            sendToClient.println("upload failed, please try again");
                            System.out.println("errors occur");
                        }
                    }else{
                        System.out.println("nonexistent data connection");
                    }
                }else if(text.startsWith("noop") || text.startsWith("NOOP")){
                    sendToClient.println("no operation");
                }else{
                    System.out.println("unknown command");
                }
            }
            receiveFromClient.close();
            sendToClient.close();
            clientSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
