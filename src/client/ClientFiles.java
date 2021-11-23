package client;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * FTP数据传输时用于多线程优化
 */

public class ClientFiles implements Callable<Boolean> {

    String path;
    File[] files;          // 传输任务
    ClientDataConnection dataConnection;

    ClientFiles(File[] files, ClientDataConnection dataConnection){
        this.files = files;
        this.dataConnection = dataConnection;
    }

    ClientFiles(String path, ClientDataConnection dataConnection){
        this.path = path;
        this.dataConnection = dataConnection;
    }

    @Override
    public Boolean call() throws IOException {
        if(files.length == 0){
            return dataConnection.downloadDirectory(path);
        }else{
            return dataConnection.uploadDirectory(files);
        }
    }
}
