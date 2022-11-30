import java.net.Socket;
import java.util.TimerTask;
import java.io.IOException;
import java.io.OutputStream;

public class LFDHeartbeatTask extends TimerTask {
    Socket gfd;
    String name;
    public LFDHeartbeatTask(Socket gfd, String name) {
        this.gfd = gfd;
        this.name = name;
    }


	@Override
	public void run() {
		OutputStream out;
        try {
            out = gfd.getOutputStream();
            out.write(("heartBeat from " + name + TCPService.END_CHAR).getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (gfd.isClosed()) {
            cancel();
        }
        
	}

}