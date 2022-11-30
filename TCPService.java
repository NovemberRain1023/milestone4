import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPService {
    public static final String SERVICE_IP = "127.0.0.1";

    public static final int SERVICE_PORT_Client = 10102;

    public static final int SERVICE_PORT_Server = 10110;


    public static final char END_CHAR = '#';

    private static int messageNumber = 0;

    private static int checkPointCount = 0;

    public static Socket lfd;

    // 1 means live, 0 means dead
    private static int myState = 1;

    private boolean isPrimary= true;

    public static void main(String[] args) {
        // checkpoint thread
        TCPService service = new TCPService();
       
        // receive message
        service.buildConnect();
        service.startService(); 
        
    }

    private void buildConnect() {
        try {
            lfd = new Socket(SERVICE_IP, TCPLFD.SERVICE_PORT);
            OutputStream out = lfd.getOutputStream();
            String connectiongMsg = "1 connect" + END_CHAR;
            out.write(connectiongMsg.getBytes());
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void startService(){
        LFDTask lfdTask = new LFDTask();
        new Thread(lfdTask, "lfdTask").start();
        ServerTask serverTask = new ServerTask();
        new Thread(serverTask, "serverTask").start();
        try {
            InetAddress address = InetAddress.getByName(SERVICE_IP);
            Socket connect = null;
            ExecutorService pool = Executors.newFixedThreadPool(5);
            try {
                ServerSocket service = new ServerSocket(SERVICE_PORT_Client,5,address);
                while(true){
                    connect = service.accept();
                    ServiceTask serviceTask = new ServiceTask(connect);
                    pool.execute(serviceTask);
                }
            }catch (Exception e){
                e.printStackTrace();
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ServiceTask implements Runnable{
        private Socket socket;

        ServiceTask(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    StringBuilder receiveMsg = new StringBuilder();
                    InputStream in = socket.getInputStream();
                    for (int c = in.read(); c != END_CHAR; c = in.read()) {
                        if(c ==-1)
                            break;
                        receiveMsg.append((char)c);
                    }
                    System.out.println("Received message: length " + receiveMsg.length());
                    System.out.println("Received message: " + receiveMsg.toString());
                    messageNumber += 1;
                    String response = " S1 " + receiveMsg.toString() + " " + isPrimary + " " + END_CHAR;
                    OutputStream out = socket.getOutputStream();
                    out.write(response.getBytes());
                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        if (!socket.isClosed())
                        socket.close();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    break;
                } 
            }
        }
    }

    public class LFDTask implements Runnable{
        LFDTask() {}
        @Override
        public void run() {
            while(true) {
                try {
                    StringBuilder receiveMsg = new StringBuilder();
                    if (TCPService.lfd.isClosed()) {
                        TCPService.lfd = new Socket(SERVICE_IP, TCPLFD.SERVICE_PORT);
                    }
                    InputStream in = TCPService.lfd.getInputStream();
                    for (int c = in.read(); c != END_CHAR; c = in.read()) {
                        if(c ==-1)
                            break;
                        receiveMsg.append((char)c);
                    }
                    System.out.println("Received message: length " + receiveMsg.length());
                    System.out.println("Received message: " + receiveMsg.toString());
                    messageNumber += 1;
                    String response = "ACKed: " + String.valueOf(messageNumber) + " " + receiveMsg.toString() + END_CHAR;
                    OutputStream out = TCPService.lfd.getOutputStream();
                    out.write(response.getBytes());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public class ServerTask implements Runnable{
        ServerTask() {}
        @Override
        public void run() {
            try {
                InetAddress address = InetAddress.getByName(SERVICE_IP);
                Socket connect = null;
                ExecutorService pool = Executors.newFixedThreadPool(5);
                try {
                    ServerSocket service = new ServerSocket(SERVICE_PORT_Server,5,address);
                    while(true){
                        connect = service.accept();
                        CheckPoint3Task serviceTask = new CheckPoint3Task(connect);
                        pool.execute(serviceTask);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static class CheckPoint3Task extends Thread {
        private Socket socket;

        CheckPoint3Task(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            while(true) {
                try {
                    String msg = "checkpoint: " + String.valueOf(TCPService.checkPointCount) + 
                         ", myState: " + String.valueOf(TCPService.myState) + 
                         ", messageNumber: " + String.valueOf(TCPService.messageNumber) + TCPService.END_CHAR;
                    checkPointCount++;
                    messageNumber++;
                    
                    OutputStream out2 = socket.getOutputStream();
                    out2.write(msg.getBytes());
                    System.out.println(msg);
                    Thread.sleep(5000);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

       
    }
}
