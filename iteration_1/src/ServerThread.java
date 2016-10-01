import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket sendReceiveSocket;
	
    public void RequestStop()
    {
    	stopRequested = true;
    }
    
    /* Closes sockets and exits. */
	public void exitGraceFully() {
		if(sendReceiveSocket != null && sendReceiveSocket.isClosed())
		{
			sendReceiveSocket.close();
		}
		System.out.println("Server: Exiting Gracefully");
		System.exit(0);
	}

}
