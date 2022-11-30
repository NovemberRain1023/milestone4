import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class TCPClient {


    private static Socket socketClient1;
    private static Socket socketClient2;
    private static Socket socketClient3;
    private static Set<String> set = new HashSet();

    private static int requestNum = 0;
    public static void main(String[] args) {
        TCPClient TCPClient = new TCPClient();
        
        try {
            socketClient1 = new Socket(TCPService.SERVICE_IP, TCPService.SERVICE_PORT_Client);
            socketClient2 = new Socket(TCPService2.SERVICE_IP, TCPService2.SERVICE_PORT);
            socketClient3 = new Socket(TCPService3.SERVICE_IP, TCPService3.SERVICE_PORT);
        } catch (IOException e) {
            System.err.println("Initialize socket client failed");
        }
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat format = new SimpleDateFormat("hh-MM-ss");
        Communicate thread1 = new Communicate(socketClient1);
        Communicate thread2 = new Communicate(socketClient2);
        Communicate thread3 = new Communicate(socketClient3);
        thread1.start();
        thread2.start();
        thread3.start();
  
        while (true) {
            
            String msg = scanner.nextLine();
            if("#".equals(msg))
                break;
            msg = "Request number " + requestNum++ + " " + msg + " with client 1 " + TCPService.END_CHAR;
            System.out.println("msg: " + msg);
            System.out.println("send time : " + format.format(new Date()) + " with client 1");
            
            try {
                OutputStream out1 = socketClient1.getOutputStream();
                out1.write(msg.getBytes());
                OutputStream out2 = socketClient2.getOutputStream();
                out2.write(msg.getBytes());
                OutputStream out3 = socketClient3.getOutputStream();
                out3.write(msg.getBytes());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
    }

    public static class Communicate extends Thread {
        public Socket socketClient;
    
        Communicate(Socket socketClient) {
            this.socketClient = socketClient;
        }
        @Override
        public void run() {
            SimpleDateFormat format = new SimpleDateFormat("hh-MM-ss");
            while (true) {
                StringBuilder receiveMsg = new StringBuilder();
                try {
                    if (socketClient != null) {
                        InputStream in = socketClient.getInputStream();
                        for (int c = in.read(); c != TCPService.END_CHAR; c = in.read()) {
                            if(c==-1)
                                break;
                            receiveMsg.append((char)c);
                        }
                        String receivedMsg = receiveMsg.toString();
                        if (receivedMsg.contains("true")) {
                            System.out.println("C1 " + receivedMsg + "receive time : " + format.format(new Date()));
                        } else {
                            String serverNum = receivedMsg.split(" ")[1];
                            System.out.println("Discarded duplicate reply from " + serverNum);
                        }
                        // if (!set.contains(receiveMsg.toString())) {
                        //     set.add(receiveMsg.toString());
                        //     System.out.println(receiveMsg.toString());
                        //     System.out.println("receive time : " + format.format(new Date()));
                        // }
                    } else {
                        System.out.println("Client is null");
                    }
                    
                    
                    
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    
        
    }

    
}

