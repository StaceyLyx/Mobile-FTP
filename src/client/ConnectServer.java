package client;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * FTP客户端链接服务器，处理命令信息
 */

public class ConnectServer {

    BufferedReader receiveFromServer;    // 接受服务器的信息输入流
    PrintWriter sendToServer;     // 客户端发送给服务器的信息输出流

    ConnectServer(PrintWriter sentToServer, BufferedReader receiveFromServer){
        this.receiveFromServer = receiveFromServer;
        this.sendToServer = sentToServer;
    }

    public ClientDataConnection port(String portStr){
        // 打开客户端该端口
        ClientDataConnection dataConnection = null;
        String IP;
        int port;
        try{
            String[] address = portStr.split(",");
            IP = address[0] + "." + address[1] + "." + address[2] + "." + address[3];
            port = 256 * Integer.parseInt(address[4]) + Integer.parseInt(address[5]);
            sendToServer.println("port");    // port参数解析无误，开始给服务器指令
            sendToServer.println(IP);
            sendToServer.println(port);
            dataConnection = new ClientDataConnection(port);
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("wrong format of \"port\" command");
        } catch (IOException e){
            System.out.println("port has been used");
        }
        return dataConnection;
    }

    public ClientDataConnection pasv(String synObject, Receive receive) throws IOException {
        receive.stopNow(true);
        String ip;
        String portStr;
        synchronized (synObject){
            ip = receiveFromServer.readLine();
            portStr = receiveFromServer.readLine();
            synObject.notify();
            receive.startNow(true);
        }
        sendToServer.println("continue");
        int port = 0;
        for(int i = 0; i < portStr.length(); ++i){
            port = port * 10 + (portStr.charAt(i) - '0');
        }
        return new ClientDataConnection(ip, port);
    }

    public boolean uploadToServer(String instruction, ClientDataConnection dataConnection, ClientDataConnection dataConnectionB) throws ExecutionException, InterruptedException {
        StringBuilder pathname = new StringBuilder();
        String[] str = instruction.split(" ");
        if(str.length == 1) throw new IndexOutOfBoundsException();
        for(int i = 1; i < str.length; ++i){
            pathname.append(str[i]);
            if(i != str.length - 1){
                pathname.append(" ");
            }
        }
        File file = new File(pathname.toString());
        if(!file.exists()){
            sendToServer.println("stop");
            return false;
        }
        if(file.isDirectory()){
            // 传输文件夹：优化————多线程传输
            sendToServer.println(file.getName());   // 新建文件夹
            File[] files = file.listFiles();
            File[] filesA;
            File[] filesB;
            assert files != null;
            if(files.length % 2 == 0){
                // 偶数
                filesA = new File[files.length / 2];
                filesB = new File[files.length / 2];
                System.arraycopy(files, 0, filesA, 0, files.length / 2 - 1);
                System.arraycopy(files, files.length / 2, filesA, 0, files.length / 2);
            }else{
                // 奇数
                filesA = new File[files.length / 2];
                filesB = new File[files.length / 2 + 1];
                System.arraycopy(files, 0, filesA, 0, files.length / 2 - 1);
                System.arraycopy(files, files.length / 2, filesA, 0, files.length / 2 + 1);
            }
            ClientFiles clientFilesA = new ClientFiles(filesA, dataConnection, sendToServer);
            ClientFiles clientFilesB = new ClientFiles(filesB, dataConnection, sendToServer);
            FutureTask<Boolean> fileResultA = new FutureTask<>(clientFilesA);
            FutureTask<Boolean> fileResultB = new FutureTask<>(clientFilesB);
            new Thread(fileResultA).start();
            new Thread(fileResultB).start();
            return fileResultA.get() && fileResultB.get();
        }else{
            // 传输单独文件
            return dataConnection.uploadFile(file, sendToServer);
        }
    }

    public boolean downloadFromServer(ClientDataConnection dataConnection, String synObject,Receive receive) throws IOException {
        boolean flag;
        receive.stopNow(true);
        synchronized (synObject){
            flag = dataConnection.download(receiveFromServer);
            synObject.notify();
            receive.startNow(true);
        }
        sendToServer.println("continue");
        return flag;
    }

    public void typeFromServer(String typeStr,ClientDataConnection dataConnection) throws FileNotFoundException {
        dataConnection.setType(typeStr);
    }
}