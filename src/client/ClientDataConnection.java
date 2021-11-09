package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientDataConnection{

    int port;
    ServerSocket socket;
    Socket serverSocket;

    ClientDataConnection(int port) throws IOException {
        this.port = port;
        socket = new ServerSocket(port);
        serverSocket = socket.accept();    // 等待客户端来连接
    }

}
