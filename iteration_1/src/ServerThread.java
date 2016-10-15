import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import ui.ConsoleUI;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket sendReceiveSocket;
	protected ConsoleUI console;
	
	public ServerThread(ThreadGroup group, String name, ConsoleUI console)
	{
		super(group,name);
		this.console=console;
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
		console.print("Server: Exiting Gracefully");
	}
	
	protected void printReceivedPacket(DatagramPacket receivedPacket, boolean verbose){
		byte[] data = receivedPacket.getData();
		int packetSize = receivedPacket.getLength();
		console.print("Server: Received Packet");
		console.print("        Source: " + receivedPacket.getAddress());
		console.print("        Port:   " + receivedPacket.getPort());
		console.print("        Bytes:  " + packetSize);
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
		console.print("\n        Cntn:  " + (new String(data,0,packetSize))+"/n");
	}
	
	protected void printSendPacket(DatagramPacket sendPacket, boolean verbose){
		console.print("Server: Sending packet...");
		if(verbose)
		{
			byte[] data = sendPacket.getData();
			int packetSize = sendPacket.getLength();
			console.print("        Host:  " + sendPacket.getAddress());
			console.print("        Port:  " + sendPacket.getPort());
			console.print("        Bytes: " + sendPacket.getLength());
			System.out.printf("%s", "        Cntn:  ");
			for(int i = 0; i < packetSize; i++)
			{
				System.out.printf("0x%02X", data[i]);
				System.out.printf("%-2c", ' ');
			}
			console.print("");
			console.print("        Cntn:  " + (new String(data,0,packetSize)));
			
		}
	}
	
    protected void printError(DatagramPacket packet,boolean verbose){
    	console.print("Server: Error packet received");
    	console.print("From client: " + packet.getAddress());
    	console.print("From client port: " + packet.getPort());
	    console.print("Length: " + packet.getLength());
	    console.print("Error Code: " + new String(packet.getData(),
				   2,2));
	    console.print("ErrorMessage: " );
	    console.print(new String(packet.getData(),
				   4,packet.getData().length-1));
    }
    /* Send Data packet with no data
    2 bytes    2 bytes       0 bytes
    ---------------------------------
DATA  | 03    |   Block #  |    Data    |
    ---------------------------------
    */
    protected void sendNoData(DatagramPacket receivePacket,boolean verbose,int blockNumber,DatagramSocket sendReceiveSocket){
    	byte[] data = new byte[4];
    	data[0] = 0;
    	data[1] = 3;
		//Encode the block number into the response block 
		data[3]=(byte)(blockNumber & 0xFF);
		data[2]=(byte)((blockNumber >> 8)& 0xFF);
    	
    	DatagramPacket sendPacket = new DatagramPacket(data, data.length,
			     receivePacket.getAddress(), receivePacket.getPort());
	/* Exit Gracefully if the stop is requested. */
	   if(stopRequested){exitGraceFully();}
      		console.print("Server: Sending packet:");
      if(verbose){
	       console.print("To host: " + sendPacket.getAddress());
	       console.print("Destination host port: " + sendPacket.getPort());
	       
	       console.print("Length: " + sendPacket.getLength());
	       console.print("Containing: " );
	       console.print(Arrays.toString(sendPacket.getData()));
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
      		console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"/n");
      	}
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
	       		console.print("Server: Sending packet:");
	       if(verbose){
		       console.print("To host: " + sendPacket.getAddress());
		       console.print("Destination host port: " + sendPacket.getPort());
		       
		       console.print("Length: " + sendPacket.getLength());
		       console.print("Containing: " );
		       console.print(Arrays.toString(sendPacket.getData()));
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
	       		console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"/n");
	       	}
    	
    }

}
