import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPService2 {
    public static final String SERVICE_IP = "127.0.0.1";

    public static final int SERVICE_PORT = 10103;

    public static final char END_CHAR = '#';

    private int messageNumber = 0;
    
    private int primaryState = 1;

    public static Socket lfd;

    public static Socket replica;

    private boolean isPrimary = false;


    public static void main(String[] args) {
        TCPService2 service = new TCPService2();
        service.buildConnect();
        service.startService();
        
    }

    private class BuildConnectToServer implements Runnable{
       
        @Override
        public void run() {
            try {
                replica = new Socket(SERVICE_IP, TCPService.SERVICE_PORT_Server);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            while(true) {
                try {
                    InputStream inputStreamFromPrimary = replica.getInputStream();
                    if (inputStreamFromPrimary != null) {
                        StringBuilder receiveMsg = new StringBuilder();
                        InputStream in = replica.getInputStream();
                        for (int c = in.read(); c != END_CHAR; c = in.read()) {
                            if(c ==-1)
                                break;
                            receiveMsg.append((char)c);
                        }
                        
                        
                        int myStateStringBeginIndex = receiveMsg.indexOf("myState");
                        String myStateString = receiveMsg.substring(myStateStringBeginIndex + 9, myStateStringBeginIndex + 10);
                        primaryState = Integer.parseInt(myStateString);
                        System.out.println("Receive checkpoint! Update primary state to: " + primaryState);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: handle exception
                }
            }
            
        }
    }

    private void buildConnect() {
        try {
            lfd = new Socket(SERVICE_IP, TCPLFD2.SERVICE_PORT);
            OutputStream out = lfd.getOutputStream();
            String connectiongMsg = "2 connect" + END_CHAR;
            out.write(connectiongMsg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    private void startService(){
        LFDTask lfdTask = new LFDTask();
        new Thread(lfdTask, "lfdTask").start();
        BuildConnectToServer buildConnectToServer = new BuildConnectToServer();
        new Thread(buildConnectToServer, "serviceTask").start();
       
        
        try {
            InetAddress address = InetAddress.getByName(SERVICE_IP);
            Socket connect = null;
            ExecutorService pool = Executors.newFixedThreadPool(5);
            try {
                ServerSocket service = new ServerSocket(SERVICE_PORT,5,address);
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

    class ServiceTask implements Runnable{
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
                    System.out.println(receiveMsg);
                    if (receiveMsg.substring(0, 1).equals("c")) {
                        System.out.println("Receive checkpoint! Update self states...");
                        int myStateStringBeginIndex = receiveMsg.indexOf("myState");
                        String myStateString = receiveMsg.substring(myStateStringBeginIndex + 9, myStateStringBeginIndex + 10);
                        primaryState = Integer.parseInt(myStateString);
                    } else {
                        System.out.println("Received message: length " + receiveMsg.length());
                        System.out.println("Received message: " + receiveMsg.toString());
                        messageNumber += 1;
                        String response = " S2 " + receiveMsg.toString() + " " + isPrimary + " " + END_CHAR;
                        OutputStream out = socket.getOutputStream();
                        out.write(response.getBytes());
                    }
                }catch (Exception e){
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
                    InputStream in = TCPService2.lfd.getInputStream();
                    for (int c = in.read(); c != END_CHAR; c = in.read()) {
                        if(c ==-1)
                            break;
                        receiveMsg.append((char)c);
                    }
                    System.out.println("Received message: length " + receiveMsg.length());
                    System.out.println("Received message: " + receiveMsg.toString());
                    messageNumber += 1;
                    String response = "ACKed: " + String.valueOf(messageNumber) + " " + receiveMsg.toString() + END_CHAR;
                    OutputStream out = TCPService2.lfd.getOutputStream();
                    out.write(response.getBytes());
                }catch (Exception e){
                    e.printStackTrace();
                } 
            }
        }
    }
}
