package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * FTP服务器连接客户端，处理命令信息
 */

public class ConnectClient {

    BufferedReader receiveFromClient;    // 接受客户端的信息输入流
    PrintWriter sendToClient;     // 服务器发送给客户端的信息输出流

    ConnectClient(PrintWriter sendToClient, BufferedReader receiveFromClient){
        this.receiveFromClient = receiveFromClient;
        this.sendToClient = sendToClient;
    }

    public boolean login() throws IOException {
        String user = receiveFromClient.readLine();
        String pass = receiveFromClient.readLine();
        return ServerInit.serverAuthority.authorityLegal(user, pass);
    }

    public ServerDataConnection port() throws IOException {
        String IP = receiveFromClient.readLine();
        int port = Integer.parseInt(receiveFromClient.readLine());
        ServerDataConnection dataConnection = new ServerDataConnection(IP, port);
        return dataConnection;
    }

    public ServerDataConnection pasv(ServerDataConnection dataConnection) throws IOException {
        sendToClient.println("accept");
        // 服务器IP地址
        String localhostAddress = "";
        try{
            localhostAddress = String.valueOf(InetAddress.getLocalHost());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        sendToClient.println(localhostAddress.split("/")[1]);
        // 服务器端口
        int port = (int)(3000 + Math.random() * (50000 - 3000 + 1));
        sendToClient.println(port);
        String check = receiveFromClient.readLine();
        if(check.equals("continue")){
            dataConnection = new ServerDataConnection(port);
        }
        return dataConnection;
    }

    public boolean uploadFromClient(ServerDataConnection dataConnection, ServerDataConnection dataConnectionB){
        try{
            String info = receiveFromClient.readLine();
            if(info.equals("ready")){
                // 传输特定文件
                String filename = receiveFromClient.readLine();   // 获取文件名
                dataConnection.uploadFile(filename, receiveFromClient);
                return true;
            }else if(info.equals("stop")){
                // 异常停止
                return false;
            }else{
                // 传输文件夹及内部文件
                String path = ServerInit.ftpPath + "/Upload/" + info + "/";
                // 新建文件夹
                File file = new File(ServerInit.ftpPath + "/Upload/" + info);
                if (!file.exists()) {
                    file.isDirectory();
                    file.mkdir();
                }
                ServerFiles serverFilesA = new ServerFiles(path, dataConnection, receiveFromClient);
                ServerFiles serverFilesB = new ServerFiles(path, dataConnectionB, receiveFromClient);
                FutureTask<Boolean> fileResultA = new FutureTask<>(serverFilesA);
                FutureTask<Boolean> fileResultB = new FutureTask<>(serverFilesB);
                new Thread(fileResultA).start();
                new Thread(fileResultB).start();
                if(fileResultA.get() && fileResultB.get()){
                    dataConnection.close();    // 关闭数据链接
                    dataConnectionB.close();
                    return true;
                }else{
                    System.out.println("client error");
                    return false;
                }
            }
        }catch (IndexOutOfBoundsException e){
            System.out.println("command needs parameter");
            return false;
        }catch (IOException e){
            sendToClient.println("upload failed, please try again");
            System.out.println("errors occur");
            return false;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downloadToClient(String text, ServerDataConnection dataConnection){
        try{
            sendToClient.println("accept");
            StringBuilder pathname = new StringBuilder();    // 解析下载文件路径
            String[] str = text.split(" ");
            if(str.length == 1) throw new IndexOutOfBoundsException();
            for(int i = 1; i < str.length; ++i){
                pathname.append(str[i]);
                if(i != str.length - 1){
                    pathname.append(" ");
                }
            }
            if(dataConnection.download(pathname.toString(), sendToClient)){
                dataConnection.close();    // 关闭数据链接
                return true;
            }else{
                receiveFromClient.readLine();
                System.out.println("errors occur");
                return false;
            }
        }catch (IndexOutOfBoundsException e){
            System.out.println("command needs parameter");
            return false;
        }catch (IOException e){
            System.out.println("errors occur");
            return false;
        }
    }

}
