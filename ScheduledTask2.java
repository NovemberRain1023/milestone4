import java.util.TimerTask;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * @author Dhinakaran P.
 */
// Create a class extends with TimerTask
public class ScheduledTask2 extends TimerTask {

	Date now; // to display current time
    Socket client;
	String ip;
	int port;
    Socket gfd;
    public ScheduledTask2(Socket client, String ip, int port, Socket gfd) {
        this.client = client;
		this.ip = ip;
		this.port = port;
        this.gfd = gfd;
    }

	// Add your task here
	public void run() {
		try {
			now = new Date(); // initialize date
			System.out.println("Time is :" + now); // Display current time
			SimpleDateFormat format = new SimpleDateFormat("hh-MM-ss");
			String msg = "heartbeat checking...";
			System.out.println("Heart beats send time : " + format.format(new Date()));
			send(ip,port, msg);
			TCPLFD2.count += 1;
			if (TCPLFD2.count  > 3) {
				System.out.println("Server no response");
                OutputStream out = gfd.getOutputStream();
                out.write(("delete replica S2" + TCPService.END_CHAR).getBytes());
				TCPLFD2.count = 0;
				// if no response, close the connection and cancel the task
				if (!client.isClosed()) {
					client.close();
				}
				
				cancel();
			}
		} catch (Exception e) {
			// System.out.println(e.getMessage());
		}
	}

    private void send(String ip, int port, String msg){
        msg = msg+TCPService.END_CHAR;

        try {

            OutputStream out = client.getOutputStream();
            out.write(msg.getBytes());
        }catch (Exception e){
            // e.printStackTrace();
        }
    }
}