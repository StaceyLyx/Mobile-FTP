package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ServerFiles implements Callable<Boolean> {

    String path;
    ServerDataConnection dataConnection;
    BufferedReader receiveFromClient;

    ServerFiles(String path, ServerDataConnection dataConnection, BufferedReader receiveFromClient){
        this.path = path;
        this.dataConnection = dataConnection;
        this.receiveFromClient = receiveFromClient;
    }

    @Override
    public Boolean call() throws IOException {
        return dataConnection.uploadDirectory(path, receiveFromClient);
    }
}
