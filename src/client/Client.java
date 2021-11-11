package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

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

        // 与服务器交互的输入输出流
        try{
            String synObject = "";   // 线程同步资源
            String instruction;
            BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            sendToServer = new PrintWriter(os, true);     // 发送给服务器的信息流
            receiveFromServer = new BufferedReader(new InputStreamReader(is));    // 接收服务器的信息流
            Receive receive = new Receive(is, os, synObject);   // FTP服务器的反馈监听线程
            Thread receiveThread = new Thread(receive);   // 建立接收服务器数据的线程
            receiveThread.setDaemon(true);   // 线程随主程序的终止而终止
            receiveThread.start();

            // 进入用户操作
            ConnectServer connectServer = new ConnectServer(sendToServer, receiveFromServer);
            ClientDataConnection dataConnection = null;
            while(true){
                instruction = keyboardIn.readLine();
                if(instruction.startsWith("quit") || instruction.startsWith("QUIT")){
                    sendToServer.println("quit");
                    System.out.println("bye");
                    break;
                }else if(instruction.startsWith("port") || instruction.startsWith("PORT")){
                    // 主动模式：告知服务器数据传输的IP地址与端口号，客户端打开该端口等待服务器进行链接
                    // 本机IP地址为：192.168.219.1
                    // port 192,168,219,1,(端口号),(端口号)
                    try{
                        dataConnection = connectServer.port(instruction.split(" ")[1]);
                    }catch (ArrayIndexOutOfBoundsException e){
                        System.out.println("wrong format of \"port\" command");
                    }
                }else if(instruction.startsWith("pasv") || instruction.startsWith("PASV")){
                    // 修改为被动模式
                }else if(instruction.startsWith("user") || instruction.startsWith("USER")){
                    // 用户登录用户名
                }else if(instruction.startsWith("pass") || instruction.startsWith("PASS")){
                    // 用户登录密码
                }else if(instruction.startsWith("type") || instruction.startsWith("TYPE")){
                    // 切换传输模式
                }else if(instruction.startsWith("mode") || instruction.startsWith("MODE")){
                    // 切换传输模式
                }else if(instruction.startsWith("stru") || instruction.startsWith("STRU")){
                    // 设置文件传输结构
                }else if(instruction.startsWith("retr") || instruction.startsWith("RETR")){
                    // 下载文件：从服务器下载文件
                    // retr ./Share/Download/(filename)
                    sendToServer.println(instruction);
                    if(dataConnection != null){
                        try{
                            connectServer.downloadFromServer(instruction, dataConnection);
                            sendToServer.println("finish");
                            dataConnection.close();   // 传输完毕，关闭数据链接
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
                    sendToServer.println(instruction);
                    if(dataConnection != null){
                        try{
                            connectServer.uploadToServer(instruction, dataConnection);
                            sendToServer.println("finish");
                            dataConnection.close();   // 传输完毕，关闭数据链接
                        }catch (IndexOutOfBoundsException e){
                            System.out.println("parameter missed");
                        }catch (IOException e){
                            System.out.println("upload failed, please try again.");
                        }
                    }else{
                        System.out.println("no data connection found");
                    }
                }else if(instruction.startsWith("noop") || instruction.startsWith("NOOP")){
                    // 无操作
                }else{
                    System.out.println("wrong command");
                }
            }
            keyboardIn.close();
            is.close();
            os.close();
            receiveFromServer.close();
            sendToServer.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
