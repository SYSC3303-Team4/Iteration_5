import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket receiveSocket;
	protected DatagramSocket sendSocket;
	
    public void RequestStop()
    {
    	stopRequested = true;
    }
    
    /* Closes sockets and exits. */
	public void exitGraceFully() {
		if(sendSocket.isClosed())
		{
			sendSocket.close();
		}
		
		if(receiveSocket.isClosed())
		{
			receiveSocket.close();
		}
		return;
	}

}
