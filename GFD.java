import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GFD {
    public static final String SERVICE_IP = "127.0.0.1";

    public static final int SERVICE_PORT = 10108;

    public static final char END_CHAR = '#';

    private int messageNumber = 0;

    private int serverNumber = 0;

    private boolean isServer1Live = false;
    private boolean isServer2Live = false;
    private boolean isServer3Live = false;

    private static Socket rm;

    public GFD() {
        new ServiceTask(null).printLiveServer();
    }
    public static void main(String[] args) {
        GFD service = new GFD();
        service.startService();
    }

    private void startService() {
        try {
            rm = new Socket(RM.SERVICE_IP,RM.SERVICE_PORT);

            InetAddress address = InetAddress.getByName(SERVICE_IP);
            Socket connect = null;
            ExecutorService pool = Executors.newFixedThreadPool(5);
            try (ServerSocket service = new ServerSocket(SERVICE_PORT,5,address)){
                while(true){
                    connect = service.accept();
                    System.out.println("service accept***********");
                    ServiceTask serviceTask = new ServiceTask(connect);
                    pool.execute(serviceTask);
                }
            }catch (Exception e){
                e.printStackTrace();
        
                
            }finally {
                if(connect!=null)
                    connect.close();
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
                    System.out.println("*******");
                    InputStream in = socket.getInputStream();
                    System.out.println("*******1");
                    // System.out.println(in.read());
                    for (int c = in.read(); c != END_CHAR; c = in.read()) {
                        if(c ==-1)
                            break;
                        receiveMsg.append((char)c);
                    }
                    System.out.println("*******2");
                    System.out.println("Received message: " + receiveMsg.toString());

                    // determine whether is a add message or delete message
                    String[] wordList = receiveMsg.toString().split(" ");
                    String currMachineNumber = wordList[2];
                    if (wordList[0].equals("add")) {
                        serverNumber++;
                        checkServeAlive(currMachineNumber);
                        printLiveServer();
                        OutputStream out = rm.getOutputStream();
                        out.write(("add replica " + currMachineNumber + END_CHAR).getBytes());
                    } else if (wordList[0].equals("delete")) {
                        serverNumber--;
                        checkServeDead(currMachineNumber);
                        printLiveServer();
                        OutputStream out = rm.getOutputStream();
                        out.write(("delete replica " + currMachineNumber + END_CHAR).getBytes());
                    } else if (wordList[0].equals("heartBeat")){
                        System.out.println("Received heartBeat");
                    } else {
                        System.out.println("invalid operation");
                    }
                    
                    messageNumber += 1;
                    String response = "ACKed: " + String.valueOf(messageNumber) + " " + receiveMsg.toString() + END_CHAR;
                    OutputStream out = socket.getOutputStream();
                    out.write(response.getBytes());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            // }finally {
            //    if(socket!=null)
            //        try {
            //            socket.close();
            //        } catch (IOException e) {
            //            e.printStackTrace();
            //        }
            // }
        }

        private void printLiveServer() {
            StringBuffer stringBuffer = new StringBuffer();
            if (isServer1Live) {
                if (stringBuffer.length() != 0) {
                    stringBuffer.append(", ");
                }
                stringBuffer.append("S1");
            }
            if (isServer2Live) {
                if (stringBuffer.length() != 0) {
                    stringBuffer.append(", ");
                }
                stringBuffer.append("S2");
            }
            if (isServer3Live) {
                if (stringBuffer.length() != 0) {
                    stringBuffer.append(", ");
                }
                stringBuffer.append("S3");
            }
            if (stringBuffer.length() != 0) {
                stringBuffer.append(".");
            }
            System.out.println("GFD: " + serverNumber + " members: " + stringBuffer.toString());
        }

        private void checkServeAlive(String currMachineNumber) {
            if (currMachineNumber.equals("S1")) {
                isServer1Live = true;
            } else if (currMachineNumber.equals("S2")) {
                isServer2Live = true;
            } else if (currMachineNumber.equals("S3")) {
                isServer3Live = true;
            }
        }

        private void checkServeDead(String currMachineNumber) {
            if (currMachineNumber.equals("S1")) {
                isServer1Live = false;
            } else if (currMachineNumber.equals("S2")) {
                isServer2Live = false;
            } else if (currMachineNumber.equals("S3")) {
                isServer3Live = false;
            }
        }
    }
}
