package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

    public ClientDataConnection port(String portStr, boolean B){
        // 打开客户端该端口
        ClientDataConnection dataConnection = null;
        String IP;
        int port;
        try{
            String[] address = portStr.split(",");
            IP = address[0] + "." + address[1] + "." + address[2] + "." + address[3];
            port = 256 * Integer.parseInt(address[4]) + Integer.parseInt(address[5]);
            if(B){
                port += 1;
            }
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

    public boolean uploadToServer(String instruction, ClientDataConnection dataConnection){
        StringBuilder pathname = new StringBuilder();
        String[] str = instruction.split(" ");
        if(str.length == 1) throw new IndexOutOfBoundsException();
        for(int i = 1; i < str.length; ++i){
            pathname.append(str[i]);
            if(i != str.length - 1){
                pathname.append(" ");
            }
        }
        return dataConnection.upload(pathname.toString(), sendToServer);
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
