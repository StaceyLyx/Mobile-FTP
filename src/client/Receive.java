package client;

import java.io.*;

/**
 * 该线程的作用为实时监听服务器端的反馈信息，并显示在客户端界面
 */

public class Receive implements Runnable{

    String synObject;
    BufferedReader receiveFromServer;
    PrintWriter sentToServer;
    InputStream is;
    OutputStream os;
    boolean isStop = false;   // 控制线程的状态

    Receive(InputStream is, OutputStream os, String synObject){
        this.is = is;
        this.os = os;
        this.synObject = synObject;
    }

    // 将线程暂停运行
    public void stopNow(boolean isStop){
        this.isStop = isStop;
    }
    // 将线程恢复运行
    public void startNow(boolean isStart){
        this.isStop = !isStart;
    }

    @Override
    public void run() {
        try{
            String text;
            receiveFromServer = new BufferedReader(new InputStreamReader(is));
            sentToServer = new PrintWriter(os, true);
            synchronized (synObject){
                while(true){
                    if(!isStop){
                        text = receiveFromServer.readLine();   // 接收服务器的控制信息
                        if(text == null || text.equals("")){
                            break;
                        }else{
                            System.out.println(text);   // 显示服务器的反馈信息
                        }
                    }else if(isStop){
                        System.out.println("Data Channel started");
                        synObject.wait();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
