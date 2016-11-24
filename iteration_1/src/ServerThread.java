import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import ui.ConsoleUI;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket sendReceiveSocket;
	protected ConsoleUI console;
	//INIT socket timeout variables
	protected static final int TIMEOUT = 5; //Seconds
	protected static final int MAX_TIMEOUTS = 5;
	protected int timeouts = 0;
	protected boolean retransmit = false;
	protected int blockNum = 1;
	protected boolean timeoutFlag = false;
	protected DatagramPacket sendPacket;
	protected DatagramPacket requestPacket;
	protected boolean retransmitDATA;
	protected boolean retransmitACK;
	protected long startTime;
	protected boolean verbose;
	protected boolean connectionEstablished;

	protected boolean errorFlag=false;
	protected int clientTID; 
	
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
		console.print("Server: Closing thread.");
	} 
	
	protected void printReceivedPacket(DatagramPacket receivedPacket, boolean verbose){
		console.print("Server: Received packet...");
		if(verbose){
			byte[] data = receivedPacket.getData();
			int packetSize = receivedPacket.getLength();
	
			console.printIndent("Source: " + receivedPacket.getAddress());
			console.printIndent("Port:      " + receivedPacket.getPort());
			console.printIndent("Bytes:   " + packetSize);
			console.printByteArray(data, packetSize);
			console.printIndent("Cntn:  " + (new String(data,0,packetSize)));
		}
	}
	
	protected void printSendPacket(DatagramPacket sendPacket, boolean verbose){
		console.print("Server: Sending packet...");
		if(verbose)
		{
			byte[] data = sendPacket.getData();
			int packetSize = sendPacket.getLength();

			console.printIndent("Source: " + sendPacket.getAddress());
			console.printIndent("Port:      " + sendPacket.getPort());
			console.printIndent("Bytes:   " + packetSize);
			console.printByteArray(data, packetSize);
			console.printIndent("Cntn:  " + (new String(data,0,packetSize)));
			
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
				   4,packet.getLength()-1));
    }
    /* Send Data packet with no data
    2 bytes    2 bytes       0 bytes
    ---------------------------------
DATA  | 03    |   Block #  |    Data    |
    ---------------------------------
    */
    protected void sendNoData(DatagramPacket requestPacket,boolean verbose,int blockNumber,DatagramSocket sendReceiveSocket){
    	byte[] data = new byte[4];
    	data[0] = 0;
    	data[1] = 3;
		//Encode the block number into the response block 
		data[3]=(byte)(blockNumber & 0xFF);
		data[2]=(byte)((blockNumber >> 8)& 0xFF);
    	
    	DatagramPacket sendPacket = new DatagramPacket(data, data.length,
			     requestPacket.getAddress(), requestPacket.getPort());
	/* Exit Gracefully if the stop is requested. */
	   if(stopRequested){exitGraceFully();}
       console.print("Server: Sending packet:");
       printSendPacket(sendPacket, verbose);

      	try {
      		sendReceiveSocket.send(sendPacket);
      	} catch (IOException e) {
      		e.printStackTrace();
      		System.exit(1);
      	}
      	long startTime = System.currentTimeMillis();
      	/* Exit Gracefully if the stop is requested. */
      	if(stopRequested){exitGraceFully();}
      	if(verbose){
      		console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"\n");
      	}
    }
    
    
    //Build an Error Packet with format :
    /*
    2 bytes  2 bytes        string    1 byte
    ----------------------------------------
ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
    ----------------------------------------
    */
    protected void buildError(int errorCode,DatagramPacket requestPacket, boolean verbose, String errorInfo){
    	int errorSizeFactor = 5;
    	
    	String errorMsg = new String("Unknown Error.");
    	switch(errorCode){
	    	case 1:
	    		errorCode = 1;
	    		console.print("Server: File not found, sending error packet");
	    		errorMsg = "File not found: " + errorInfo;
	    		break;
	    	case 2: 
	    		errorCode = 2;
	    		console.print("Server: Access violation, sending error packet");
	    		errorMsg = "Access violation: " + errorInfo;
	    		break;
	    	case 3: 
	    		errorCode = 3;
	    		console.print("Server: Disk full or allocation exceeded, sending error packet");
	    		errorMsg = "Disk full or allocation exceeded: " + errorInfo;
	    		break;
	    	case 4:
	    		errorCode = 4;
	    		console.print("Illegal TFTP operation");
	    		errorMsg = "Illegal TFTP operation: " + errorInfo;
	    		break;
	    	case 5:
	    		errorCode = 5;
	    		console.print("Unknown Transfer ID");
	    		errorMsg = "Unknown Transfer ID: " + errorInfo;
	    		break;
	    	case 6: 
	    		errorCode = 6;
	    		console.print("Server: File already exists, sending error packet");
	    		errorMsg = "File already exists: " + errorInfo;
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
				     requestPacket.getAddress(), requestPacket.getPort());
		/* Exit Gracefully if the stop is requested. */
		   if(stopRequested){exitGraceFully();}
		   printSendPacket(sendPacket,verbose);

	       	try {
	       		sendReceiveSocket.send(sendPacket);
	       	} catch (IOException e) {
	       		e.printStackTrace();
	       		System.exit(1);
	       	}
	       	/* Exit Gracefully if the stop is requested. */
	       	if(stopRequested){exitGraceFully();}
	       	if(verbose){
	       		console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"\n");
	       	}
    	
    }
    
   
  //receive ACK
  	public boolean receiveACK()
  	{	
  		timeoutFlag=false;
  		//Encode the block number into the response block 
  		byte[] blockArray = new byte[2];
  		blockArray[1]=(byte)(blockNum & 0xFF);
  		blockArray[0]=(byte)((blockNum >> 8)& 0xFF);
  		console.print("Server: Waiting to receive packet");


  		//receive ACK
  		try {
  			//receiveDATA();
  			sendReceiveSocket.receive(requestPacket);
  			retransmit=false;
  		} catch(SocketTimeoutException e){
  			//Retransmit every timeout
  			//Quite after 5 timeouts
  			timeoutFlag=true;
  			if(System.currentTimeMillis() -startTime > TIMEOUT)
  			{
  				timeouts++;
  				if(timeouts == MAX_TIMEOUTS){
  					exitGraceFully();
  					requestStop();
  					errorFlag=true;
  					return false;
  				}
  				console.print("TIMEOUT EXCEEDED: SETTING RETRANSMIT TRUE");
  				retransmitDATA = true;
  				return true;
  			}
  			return false;


  		} catch (IOException e) {
  			e.printStackTrace();
  			return false;
  		} 
  		//analyze ACK for format
  		if (verbose)
  		{
  			console.print("Client: Checking ACK...");
  			printReceivedPacket(requestPacket, verbose);
  		}
  		byte[] data = requestPacket.getData();
  		if(connectionEstablished){
	  		if(requestPacket.getPort() != clientTID){
	  			buildError(5,requestPacket,verbose,"Unexpected TID");
	  			console.print("Unexpected TID");
	  			errorFlag=true;
				return false;
	  		}
  		}
  		//check ACK for validity
		if(data.length > 4){
			buildError(4,requestPacket, verbose,"Length of the ACK is over 4.");
			errorFlag=true;
			return false;
		}
  		if(data[0] == 0 && data[1] == 4){

  			//Check if the blockNumber corresponds to the expected blockNumber
  			if(blockArray[1] == data[3] && blockArray[0] == data[2]){
  				blockNum++;
  				timeouts=0;
  				retransmitDATA=false;
  			}
  			else{
  				if (verbose)
  		  		{
  		  			console.print("Received Duplicate.");
  		  		}
  				if(System.currentTimeMillis() -startTime > TIMEOUT)
  				{
  					timeouts++;
  					if(timeouts == MAX_TIMEOUTS){
  						exitGraceFully();
  						errorFlag=true;
  	  					return false;
  					}
  					retransmitDATA=true;
  					console.print("TIMEOUT EXCEEDED: SETTING RETRANSMIT TRUE");
  					return true;
  				}
  				return false;
  			}
  		}
  		else{
  			//ITERATION 5 ERROR
  			//Invalid TFTP code
  			buildError(4,requestPacket,verbose,"Not the Expected DATA packet.");
  			errorFlag=true;
			return false;
  		}
  		return true;
  	}

  //receive ACK
  	public boolean receiveDATA()
  	{	
  		timeoutFlag=false;
  		//Encode the block number into the response block 
  		byte[] blockArray = new byte[2];
  		blockArray[1]=(byte)(blockNum & 0xFF);
  		blockArray[0]=(byte)((blockNum >> 8)& 0xFF);
  		try {
  			//receiveDATA();
  			sendReceiveSocket.receive(requestPacket);
  			retransmit=false;
  		} catch(SocketTimeoutException e){
  			//Retransmit every timeout
  			//Quite after 5 timeouts

  			if(System.currentTimeMillis() -startTime > TIMEOUT)
  			{
  				timeouts++;
  				timeoutFlag=true;
  				if(timeouts == MAX_TIMEOUTS){
  					exitGraceFully();
  					requestStop();
  					errorFlag=true;
  					return false;
  				}
  				console.print("TIMEOUT EXCEEDED: SETTING RETRANSMIT TRUE");
  				retransmitACK = true;
  				return true;
  			}
  			return false;

  		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
  		//analyze DATA for format
  		if (verbose)
  		{
  			console.print("Server: Checking DATA...");
  		}
  		byte[] data = requestPacket.getData();
  		if(connectionEstablished){
	  		if(requestPacket.getPort() != clientTID){
	  			buildError(5,requestPacket,verbose,"Unexpected TID");
	  			console.print("Unexpected TID");
	  			errorFlag=true;
				return false;
	  		}
  		}
		if(data.length > 516){
			buildError(4,requestPacket, verbose,"Length of the DATA packet is over 516.");
			errorFlag=true;
			return false;
		}

  		//check if data
  		if(data[0] == 0 && data[1] == 3){

  			//Check if the blockNumber corresponds to the expected blockNumber
  			if(blockArray[1] == data[3] && blockArray[0] == data[2]){
  				blockNum++;
  				timeouts=0;
  				retransmitACK=false;
  			}
  			else{
  				if (verbose)
  		  		{
  		  			console.print("Received Duplicate Packet: ");
  		  			printReceivedPacket(requestPacket, verbose);
  		  		}
  				if(System.currentTimeMillis() -startTime > TIMEOUT)
  				{
  					timeouts++;
  					if(timeouts == MAX_TIMEOUTS){
  						//close();
  						errorFlag=true;
  	  					return false;
  					}
  					console.print("TIMEOUT EXCEEDED: SETTING RETRANSMIT TRUE");
  					retransmitACK=true;
  					return true;
  				}
  				return false;
  			}
  		}
  		else if(data[0] == 0 && data[1] == 5){
  			printError(requestPacket, verbose);
  			errorFlag=true;
			return false;
  		}
  		else{
  			buildError(5,requestPacket,verbose,"OpCode is invalid");
  			errorFlag=true;
			return false;
  		}
  		return true;
  	}
  	protected void requestStop()
  	{
  		stopRequested=true;
  	}
  	
  	@Override 
  	public void interrupt()
  	{
  		super.interrupt();
  		requestStop();
  	}

}
