import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPLFD {
    public static final int SERVICE_PORT = 10109;

    public static int count = 0;
    public static Socket client;
    public static Socket gfd;

    // Initialization
    public TCPLFD() {}
    
    public static void main(String[] args) {
        TCPLFD lfd = new TCPLFD();
        Timer time = new Timer(); // Instantiate Timer Object
        try {
            gfd = new Socket(GFD.SERVICE_IP,GFD.SERVICE_PORT);
            LFDHeartbeatTask lfdHeartbeatTask = new LFDHeartbeatTask(gfd, "LFD1");
            time.schedule(lfdHeartbeatTask, 0, 5000);
            InetAddress address = InetAddress.getByName(TCPService.SERVICE_IP);
            Socket connect = null;
            ExecutorService pool = Executors.newFixedThreadPool(5);
            try {
                ServerSocket service = new ServerSocket(SERVICE_PORT,5,address);
                while(true) {
                    StringBuilder receiveMsg = new StringBuilder();
                    if (connect == null) {
                        connect = service.accept();
                        System.out.println("Service 1 connected");
                        OutputStream out = gfd.getOutputStream();
                        out.write(("add replica S1" + TCPService.END_CHAR).getBytes());
                        ScheduledTask st = new ScheduledTask(connect, TCPService.SERVICE_IP,TCPService.SERVICE_PORT_Client, gfd); // Instantiate SheduledTask class
                        time.schedule(st, 0, 5000); // Create Repetitively task for every 1 secs
                    }
                        
                    try {
                        InputStream in = connect.getInputStream();
                        for (int c = in.read(); c != TCPService.END_CHAR; c = in.read()) {
                         if(c==-1)
                             break;
                         receiveMsg.append((char)c);
                         }
                         if (receiveMsg != null && receiveMsg.length() != 0) {
                             System.out.println(receiveMsg.toString() + " length with: " + receiveMsg.length());
                             TCPLFD.count = 0;
                         }
         
                     } catch (Exception e) {
                        //System.out.println(e.getMessage());
                        connect = null;
                     }
                    
                    
                    // send message to GFD
                   
                   
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("catch");
        }

        // while (true) {
        //     StringBuilder receiveMsg = new StringBuilder();
        //     try {
        //        InputStream in = TCPLFD.client.getInputStream();
        //        for (int c = in.read(); c != TCPService.END_CHAR; c = in.read()) {
        //         if(c==-1)
        //             break;
        //         receiveMsg.append((char)c);
        //         }
        //         if (receiveMsg != null && receiveMsg.length() != 0) {
        //             System.out.println(receiveMsg.toString() + "length with: " + receiveMsg.length());
        //             TCPLFD.count = 0;
        //         }

        //     } catch (Exception e) {
        //         // System.out.println(e.getMessage());
        //     }
            
		// }

    }
}