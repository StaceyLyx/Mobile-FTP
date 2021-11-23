package server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ServerFiles implements Callable<Boolean> {

    String path;
    File[] files;
    ServerDataConnection dataConnection;

    ServerFiles(String path, ServerDataConnection dataConnection){
        this.path = path;
        this.dataConnection = dataConnection;
    }

    ServerFiles(File[] files, ServerDataConnection dataConnection){
        this.files = files;
        this.dataConnection = dataConnection;
    }

    @Override
    public Boolean call() throws IOException{
        if(files.length == 0){
            return dataConnection.uploadDirectory(path);
        }else{
            return dataConnection.downloadDirectory(files);
        }
    }
}
