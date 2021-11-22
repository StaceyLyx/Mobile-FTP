package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * FTP客户端
 */

public class Client {

    BufferedReader receiveFromServer;    // 接受服务器的信息输入流
    PrintWriter sendToServer;     // 客户端发送给服务器的信息输出流

    // 初始化FTP客户端
    public Socket init(String ip, int port){      // 客户端创建Socket通信，设置通信服务器的ip与port

        Socket socket = null;
        try{
            socket = new Socket(InetAddress.getByName(ip), port);
            File ClientDirectory = new File("Client Files");
            if(!ClientDirectory.exists()){
                ClientDirectory.isDirectory();
                ClientDirectory.mkdir();
            }
            File downloadDirectory = new File("./Client Files/Download");
            if(!downloadDirectory.exists()){
                downloadDirectory.isDirectory();
                downloadDirectory.mkdir();
            }
            File uploadDirectory = new File("./Client Files/Upload");
            if(!uploadDirectory.exists()){
                uploadDirectory.isDirectory();
                uploadDirectory.mkdir();
            }
        }catch (IOException e){
            System.out.println("error in FTP client setting");
            e.printStackTrace();
        }
        return socket;
    }

    // 运行FTP客户端
    public void run(Socket clientSocket){

        System.out.println("The FTP is started successfully!");
        // 与服务器交互的输入输出流
        try{
            String synObject = "control";   // 线程同步资源
            String instruction;
            BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            sendToServer = new PrintWriter(os, true);     // 发送给服务器的信息流
            receiveFromServer = new BufferedReader(new InputStreamReader(is));    // 接收服务器的信息流

//          用户需要先登录
            System.out.println("You can enter \"user\" and \"pass\" command for your login. " +
                               "If you aren't have an account, you can login anonymously.");
            String user = "", pass;
            while(true){
                instruction = keyboardIn.readLine();
                if(instruction.startsWith("user") || instruction.startsWith("USER")){
                    // 用户登录用户名
                    user = instruction.split(" ")[1];
                    if(user.equals("anonymous")){     // 匿名用户登录
                        sendToServer.println("login");
                        sendToServer.println(user);
                        sendToServer.println("");
                        receiveFromServer.readLine();
                        System.out.println("anonymous user login.");
                        break;
                    }
                }else if(instruction.startsWith("pass") || instruction.startsWith("PASS")){
                    // 用户登录密码
                    String[] str = instruction.split(" ");
                    if(str.length == 1){
                        pass = "";
                    }else{
                        pass = str[1];
                    }
                    sendToServer.println("login");
                    sendToServer.println(user);
                    sendToServer.println(pass);
                    String confirm = receiveFromServer.readLine();
                    if(confirm.equals("legal")){
                        System.out.println("FTP Login successfully!");
                        break;
                    }else{
                        System.out.println("illegal user");
                    }
                }else{
                    System.out.println("Please login first. You can use anonymous account if you don't have an account now");
                }
            }

            // 登录成功，进入用户操作
            Receive receive = new Receive(is, os, synObject);   // FTP服务器的反馈监听线程
            Thread receiveThread = new Thread(receive);   // 建立接收服务器数据的线程
            receiveThread.setDaemon(true);   // 线程随主程序的终止而终止
            receiveThread.start();
            ConnectServer connectServer = new ConnectServer(sendToServer, receiveFromServer);
            ClientDataConnection dataConnection = null;
            ClientDataConnection dataConnectionB = null;   // 辅助数据传输
            while(true){
                instruction = keyboardIn.readLine();
                if(instruction.startsWith("quit") || instruction.startsWith("QUIT")){
                    sendToServer.println("quit");
                    System.out.println("bye");
                    break;
                }else if(instruction.startsWith("port") || instruction.startsWith("PORT")){
                    // 主动模式：告知服务器数据传输的IP地址与端口号，客户端打开该端口等待服务器进行连接
                    // 本机IP地址为：192.168.219.1
                    // port 192,168,219,1,(端口号1),(端口号2)
                    try{
                        dataConnection = connectServer.port(instruction.split(" ")[1]);
                        dataConnectionB = new ClientDataConnection(dataConnection.port + 1);
                    }catch (ArrayIndexOutOfBoundsException e){
                        System.out.println("wrong format of \"port\" command");
                    }
                }else if(instruction.startsWith("pasv") || instruction.startsWith("PASV")){
                    // 被动模式：服务器任意开一个端口告知客户端，客户端进行连接
                    sendToServer.println(instruction);
                    try{
                        dataConnection = connectServer.pasv(synObject, receive);
                        dataConnectionB = new ClientDataConnection(dataConnection.ip, dataConnection.port + 1);
                    }catch (IOException e){
                        e.printStackTrace();
                        System.out.println("wrong switch to passive mode");
                    }
                }else if(instruction.startsWith("type") || instruction.startsWith("TYPE")){
                    // 切换传输模式: ASCII和BINARY
                    String type = instruction.split(" ")[1];
                    if (type.equalsIgnoreCase("ascii") || type.equalsIgnoreCase("binary")){
                        sendToServer.println(instruction);
                        sendToServer.println(type);
                    }else{
                        System.out.println("Wrong type!");
                    }
                    if (dataConnection!=null) {
                        try {
                            connectServer.typeFromServer(instruction.split(" ")[1], dataConnection);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("wrong format of \"type\" command");
                        }
                    }
                }else if(instruction.startsWith("mode") || instruction.startsWith("MODE")){
                    // 切换传输模式
                    try{
                        String mode = instruction.split(" ")[1];
                        sendToServer.println(instruction);
                        sendToServer.println(mode);
                    }catch (IndexOutOfBoundsException e){
                        System.out.println("wrong format of \"mode\" command");
                    }
                }else if(instruction.startsWith("stru") || instruction.startsWith("STRU")){
                    // 设置文件传输结构
                    try{
                        String stru = instruction.split(" ")[1];
                        sendToServer.println(instruction);
                        sendToServer.println(stru);
                    }catch (IndexOutOfBoundsException e){
                        System.out.println("wrong format of \"stru\" command");
                    }
                }else if(instruction.startsWith("retr") || instruction.startsWith("RETR")){
                    // 下载文件：从服务器下载文件
                    // retr ./Share/Download/(filename)
                    sendToServer.println(instruction);
                    if(dataConnection != null && dataConnection.on){
                        try{
                            Date time1 = new Date();
                            boolean confirm = connectServer.downloadFromServer(dataConnection, synObject, receive);
                            if(!confirm){
                                System.out.println("download failed, please try again");
                            }else{
                                Date time2 = new Date();
                                dataConnection.close();   // 传输完毕，关闭数据链接
                                dataConnectionB.close();
                                System.out.println("Transmission time: " + (time2.getTime() - time1.getTime()) + " ms");
                            }
                        }catch (IndexOutOfBoundsException e){
                            System.out.println("parameter missed");
                        }catch (IOException e){
                            System.out.println("download failed, please try again");
                        }
                    }else{
                        System.out.println("no data connection found");
                    }
                }else if(instruction.startsWith("stor") || instruction.startsWith("STOR")){
                    // 上传文件：将文件存储到服务器
                    // stor ./Client Files/Upload/(filename)
                    sendToServer.println(instruction);
                    if(dataConnection != null && dataConnection.on){
                        try{
                            Date time1 = new Date();
                            boolean confirm = connectServer.uploadToServer(instruction, dataConnection, dataConnectionB);
                            if(!confirm){
                                System.out.println("upload failed, please try again.");
                            }else{
                                Date time2 = new Date();
                                dataConnection.close();   // 传输完毕，关闭数据链接
                                System.out.println("Transmission time: " + (time2.getTime() - time1.getTime()) + " ms");
                            }
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                            sendToServer.println("stop");
                            System.out.println("parameter missed");
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println("no data connection found");
                    }
                }else if(instruction.startsWith("noop") || instruction.startsWith("NOOP")){
                    // 无操作
                    sendToServer.println(instruction);
                }else{
                    System.out.println("wrong command");
                }
            }
            is.close();
            os.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
