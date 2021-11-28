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
        return new ServerDataConnection(IP, port);
    }

    public ServerDataConnection pasv(ServerDataConnection dataConnection) throws IOException {
        sendToClient.println("accept");
        // 服务器IP地址
        sendToClient.println("10.223.137.72");
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
            switch (info) {
                case "file":
                    // 传输特定文件
                    dataConnection.uploadFile();
                    return true;
                case "stop":
                    // 异常停止
                    return false;
                case "directory":
                    // 传输文件夹及内部文件
                    String direName = receiveFromClient.readLine();
                    String path = ServerInit.ftpPath + "/Upload/" + direName + "/";
                    // 新建文件夹
                    File file = new File(ServerInit.ftpPath + "/Upload/" + direName);
                    if (!file.exists()) {
                        file.isDirectory();
                        file.mkdir();
                    }
                    ServerFiles serverFilesA = new ServerFiles(path, dataConnection);
                    ServerFiles serverFilesB = new ServerFiles(path, dataConnectionB);
                    FutureTask<Boolean> fileResultA = new FutureTask<>(serverFilesA);
                    FutureTask<Boolean> fileResultB = new FutureTask<>(serverFilesB);
                    new Thread(fileResultA).start();
                    new Thread(fileResultB).start();
                    if (fileResultA.get() && fileResultB.get()) {
                        dataConnection.close();    // 关闭数据链接
                        dataConnectionB.close();
                        return true;
                    } else {
                        System.out.println("client error");
                        return false;
                    }
                default:
                    return false;
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

    public boolean downloadToClient(String text, ServerDataConnection dataConnection, ServerDataConnection dataConnectionB){
        try{
            StringBuilder pathname = new StringBuilder();    // 解析下载文件路径
            String[] str = text.split(" ");
            if(str.length == 1) throw new IndexOutOfBoundsException();
            for(int i = 1; i < str.length; ++i){
                pathname.append(str[i]);
                if(i != str.length - 1){
                    pathname.append(" ");
                }
            }
            File file = new File(pathname.toString());
            if(!file.exists()){
                sendToClient.println("stop");
                return false;
            }
            if(file.isDirectory()){
                // 传输文件夹：优化————多线程传输
                sendToClient.println("directory");
                sendToClient.println(file.getName());   // 新建文件夹
                File[] files = file.listFiles();
                File[] filesA;
                File[] filesB;
                assert files != null;
                if(files.length % 2 == 0){
                    // 偶数
                    filesA = new File[files.length / 2];
                    filesB = new File[files.length / 2];
                    System.arraycopy(files, 0, filesA, 0, files.length / 2);
                    System.arraycopy(files, files.length / 2, filesB, 0, files.length / 2);
                }else{
                    // 奇数
                    filesA = new File[files.length / 2];
                    filesB = new File[files.length / 2 + 1];
                    System.arraycopy(files, 0, filesA, 0, files.length / 2);
                    System.arraycopy(files, files.length / 2, filesB, 0, files.length / 2 + 1);
                }
                ServerFiles serverFilesA = new ServerFiles(filesA, dataConnection);
                ServerFiles serverFilesB = new ServerFiles(filesB, dataConnectionB);
                FutureTask<Boolean> fileResultA = new FutureTask<>(serverFilesA);
                FutureTask<Boolean> fileResultB = new FutureTask<>(serverFilesB);
                new Thread(fileResultA).start();
                new Thread(fileResultB).start();
                return fileResultA.get() && fileResultB.get();
            }else{
                // 传输单独文件
                sendToClient.println("file");
                return dataConnection.downloadFile(file);
            }
        }catch (IndexOutOfBoundsException e){
            System.out.println("command needs parameter");
            return false;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

}
