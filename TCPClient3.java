import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class TCPClient3 {


    private static Socket socketClient1;
    private static Socket socketClient2;
    private static Socket socketClient3;
    private static Set<String> set = new HashSet();
    public static void main(String[] args) {
        TCPClient3 TCPClient = new TCPClient3();
        
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
        thread1.start();
  
        while (true) {
            
            String msg = scanner.nextLine() + "with time: " + format.format(new Date());
            if("#".equals(msg))
                break;
            msg = msg+ " with client 4" + TCPService.END_CHAR;
            System.out.println("msg: " + msg + "with length: " + msg.length());
            System.out.println("send time : " + format.format(new Date()) + " with client 3");
            
            try {
                OutputStream out1 = socketClient1.getOutputStream();
                out1.write(msg.getBytes());
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
                    InputStream in = socketClient.getInputStream();
                    for (int c = in.read(); c != TCPService.END_CHAR; c = in.read()) {
                        if(c==-1)
                            break;
                        receiveMsg.append((char)c);
                    }
                    if (!set.contains(receiveMsg.toString())) {
                        set.add(receiveMsg.toString());
                        System.out.println(receiveMsg.toString());
                        System.out.println("receive time : " + format.format(new Date()));
                    }
                    
                    
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    
        
    }

    
}

