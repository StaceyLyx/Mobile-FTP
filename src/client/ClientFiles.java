package client;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

/**
 * FTP数据传输时用于多线程优化
 */

public class ClientFiles implements Callable<Boolean> {

    File[] files;          // 传输任务
    ClientDataConnection dataConnection;
    PrintWriter sendToServer;

    ClientFiles(File[] files, ClientDataConnection dataConnection, PrintWriter sendToServer){
        this.files = files;
        this.dataConnection = dataConnection;
        this.sendToServer = sendToServer;
    }

    @Override
    public Boolean call() {
        return dataConnection.uploadDirectory(files, sendToServer);
    }
}
