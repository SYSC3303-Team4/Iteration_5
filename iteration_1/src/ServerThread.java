import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket sendReceiveSocket;
	
	public ServerThread(ThreadGroup group, String name)
	{
		super(group,name);
	}
	
    public void RequestStop()
    {
    	stopRequested = true;
    }
    
    /* Closes sockets and before exit. */
	public void exitGraceFully() {
		if(sendReceiveSocket != null && sendReceiveSocket.isClosed())
		{
			sendReceiveSocket.close();
		}
		System.out.println("Server: Exiting Gracefully");
	}
	
    protected void printError(DatagramPacket packet){
    	System.out.println("Server: Error packet received");
    	System.out.println("From client: " + packet.getAddress());
    	System.out.println("From client port: " + packet.getPort());
	    System.out.println("Length: " + packet.getLength());
	    System.out.println("Error Code: " + new String(packet.getData(),
				   2,2));
	    System.out.println("ErrorMessage: " );
	    System.out.println(new String(packet.getData(),
				   4,packet.getData().length-1));
    }
    
    
    //Build an Error Packet with format :
    /*
    2 bytes  2 bytes        string    1 byte
    ----------------------------------------
ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
    ----------------------------------------
    */
    protected void buildError(int errorCode,DatagramPacket receivePacket, boolean verbose){
    	int errorSizeFactor = 5;
    	
    	String errorMsg = new String("Unknown Error.");
    	switch(errorCode){
	    	case 1:
	    		errorCode = 1;
	    		errorMsg = "File not found.";
	    		break;
	    	case 2: 
	    		errorCode = 2;
	    		errorMsg = "Access violation.";
	    		break;
	    	case 3: 
	    		errorCode = 3;
	    		errorMsg = "Disk full or allocation exceeded.";
	    		break;
	    	case 6: 
	    		errorCode = 6;
	    		errorMsg = "File already exists.";
	    		break;
    	}
    	
    	byte[] data = new byte[errorMsg.length() + errorSizeFactor];
    	data[0] = 0;
    	data[1] = 5;
    	data[2] = 0;
    	data[3] = (byte)errorCode;
    	for(int c = 0; c<errorMsg.length();c++){
    		data[4+c] = errorMsg.getBytes()[c];
    	}
    	data[data.length-1] = 0;
    	
	    DatagramPacket sendPacket = new DatagramPacket(data, data.length,
				     receivePacket.getAddress(), receivePacket.getPort());
		/* Exit Gracefully if the stop is requested. */
		   if(stopRequested){exitGraceFully();}
	       		System.out.println("Server: Sending packet:");
	       if(verbose){
		       System.out.println("To host: " + sendPacket.getAddress());
		       System.out.println("Destination host port: " + sendPacket.getPort());
		       
		       System.out.println("Length: " + sendPacket.getLength());
		       System.out.println("Containing: " );
		       System.out.println(Arrays.toString(sendPacket.getData()));
	       }


	       	// Send the datagram packet to the client via a new socket.

	       	try {
	       		// Construct a new datagram socket and bind it to any port
	       		// on the local host machine. This socket will be used to
				// send UDP Datagram packets.
		       	sendReceiveSocket = new DatagramSocket();
	       	} catch (SocketException se) {
	       		se.printStackTrace();
	       		System.exit(1);
	       	}

	       	try {
	       		sendReceiveSocket.send(sendPacket);
	       	} catch (IOException e) {
	       		e.printStackTrace();
	       		System.exit(1);
	       	}
	       	/* Exit Gracefully if the stop is requested. */
	       	if(stopRequested){exitGraceFully();}
	       	if(verbose){
	       		System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
	    	  	System.out.println();
	       	}
    	
    }

}
