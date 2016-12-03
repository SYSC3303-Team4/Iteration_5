
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import ui.ConsoleUI;

public abstract class ServerThread extends Thread{
	
	protected boolean stopRequested = false;
	protected DatagramSocket sendReceiveSocket;
	protected ConsoleUI console;
	//INIT socket timeout variables
	protected static final int TIMEOUT = 5; //Seconds
	protected static final int MAX_TIMEOUTS = 5;
	protected int timeouts = 0;
	protected int blockNum = 1;
	protected boolean timeoutFlag = false;
	protected DatagramPacket sendPacket;
	protected DatagramPacket requestPacket;
	protected DatagramPacket receivePacket;
	protected boolean retransmitDATA = false;
	protected boolean retransmitACK = false;
	protected long startTime;
	protected boolean verbose;
	protected String fileName;
	protected String mode;
	protected File serverDump;
	protected String threadNumber;

	protected boolean errorFlag=false;
	protected int clientTID;
	protected InetAddress clientInet;
	
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
		console.print("Server: thread"+threadNumber+" closing.");
	} 
	
	protected void printReceivedPacket(DatagramPacket packet){
		console.print("Server: Received packet...");
		if(verbose){
			byte[] data = packet.getData();
			int packetSize = packet.getLength();
	
			console.printIndent("Source: " + packet.getAddress());
			console.printIndent("Port:      " + packet.getPort());
			console.printIndent("Bytes:   " + packetSize);
			console.printByteArray(data, packetSize);
			console.printIndent("Cntn:  " + (new String(data,0,packetSize)));
		}
	}
	
	protected void printSendPacket(DatagramPacket sendPacket){
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
	
    protected void printError(DatagramPacket packet){
		byte[] data = packet.getData();
		int packetSize = packet.getLength();
		
    	console.print("Server: Error packet received");
    	console.print("From client: " + packet.getAddress());
    	console.print("From client port: " + packet.getPort());
	    console.print("Length: " + packetSize);
	    console.print("Error Code: " + new String(data,
				   2,2));
	    console.print("ErrorMessage: " );
	    console.print(new String(data,4,packetSize-4));
    }
    /* Send Data packet with no data
    2 bytes    2 bytes       0 bytes
    ---------------------------------
DATA  | 03    |   Block #  |    Data    |
    ---------------------------------
    */
    protected void sendNoData(DatagramPacket requestPacket,int blockNumber,DatagramSocket sendReceiveSocket){
    	byte[] data = new byte[4];
    	data[0] = 0;
    	data[1] = 3;
		//Encode the block number into the response block 
		data[3]=(byte)(blockNumber & 0xFF);
		data[2]=(byte)((blockNumber >> 8)& 0xFF);
    	
    	DatagramPacket sendPacket = new DatagramPacket(data, data.length,
			     clientInet, clientTID);
       console.print("Server: Sending packet:");
       printSendPacket(sendPacket);
      	try {
      		sendReceiveSocket.send(sendPacket);
      	} catch (IOException e) {
      		e.printStackTrace();
      		System.exit(1);
      	}
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
    protected void buildError(int errorCode,DatagramPacket requestPacket, String errorInfo){
    	int errorSizeFactor = 5;
    	
    	String errorMsg = new String("Unknown Error.");
    	switch(errorCode){
	    	case 1:
	    		console.print("Server: File not found, sending error packet");
	    		errorMsg = "File not found: " + errorInfo;
	    		break;
	    	case 2: 
	    		console.print("Server: Access violation, sending error packet");
	    		errorMsg = "Access violation: " + errorInfo;
	    		break;
	    	case 3: 
	    		console.print("Server: Disk full or allocation exceeded, sending error packet");
	    		errorMsg = "Disk full or allocation exceeded: " + errorInfo;
	    		break;
	    	case 4:
	    		console.print("Illegal TFTP operation");
	    		errorMsg = "Illegal TFTP operation: " + errorInfo;
	    		break;
	    	case 5:
	    		console.print("Unknown Transfer ID");
	    		errorMsg = "Unknown Transfer ID: " + errorInfo;
	    		break;
	    	case 6: 
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

	    printSendPacket(sendPacket);

	    try {
	    	sendReceiveSocket.send(sendPacket);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	    if(verbose){
	    	console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"\n");
	    }

    }
    
   
    /**
     *  This method is used for receiving acknowledgement datagram's.
     *  It will also verify that the packet is in the correct format.
     *   
     * @return A boolean that represents the state of the receive. 
     *			If True - Receive successful
     * 			If False - Receive unsuccessful possibly try again or flag error.
     */
  	public boolean receiveACK()
  	{	
  		timeoutFlag=false;
  		/* Encode the block number into the response block. */ 
  		byte[] blockArray = new byte[2];
  		blockArray[1]=(byte)(blockNum & 0xFF);
  		blockArray[0]=(byte)((blockNum >> 8)& 0xFF);
  		console.print("Server: Waiting to receive packet");
  		
  		/* Receive ACK. */
  		try {
  			sendReceiveSocket.receive(receivePacket);
  			retransmitDATA=false;
  		} catch(SocketTimeoutException e){
  			//Retransmit every timeout
  			//Quit after 5 timeouts
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
  				startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data after retransmit (this is used for retransmits).
  				return true;
  			}
  			return false;


  		} catch (IOException e) {
  			e.printStackTrace();
  			return false;
  		} 
  		
  		if (verbose)
  		{
  			console.print("Client: Checking ACK...");
  			printReceivedPacket(receivePacket);
  		}
  		byte[] data = receivePacket.getData();
  		/* Check ACK address. */
  		if(!clientInet.equals(receivePacket.getAddress())){
  			buildError(5,receivePacket,"Invalid InetAddress");
  			console.print("Invalid InetAddress");
  			errorFlag=true;
			return false;
  		}
  		/* Check ACK port. */
  		if(receivePacket.getPort() != clientTID){
  			buildError(5,requestPacket,"Unexpected TID");
  			console.print("Unexpected TID");
  			errorFlag=true;
			return false;
  		}

		/* Check ACK OpCode. */
  		if(data[0] == 0 && data[1] == 4){
  	  		/* Check ACK length. */
  			if(requestPacket.getLength() > 4){
  				buildError(4,requestPacket,"Length of the ACK is over 4.");
  				errorFlag=true;
  				return false;
  			}

  			/* Check BlockNumber. */
  			if(blockArray[1] == data[3] && blockArray[0] == data[2]){
  				blockNum++;
  				timeouts=0;
  				retransmitDATA=false;
  			}
  			/* Received Duplicate. */
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
  					startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data after retransmit (this is used for retransmits).
  					return true;
  				}
  				return false;
  			}
  		}
  		/* Received Error, print & quit. */
  		else if(data[0] == 0 && data[1] == 5){
  			printError(requestPacket);
  			errorFlag=true;
			return false;
  		}
  		/* Received invalid opcode, send back error. */
  		else{
  			buildError(5,requestPacket,"OpCode is invalid");
  			errorFlag=true;
			return false;
  		}
  		return true;
  	}

    /**
     *  This method is used for receiving data datagram's.
     *  It will also verify that the packet is in the correct format.
     *   
     * @return A boolean that represents the state of the receive. 
     *			If True - Receive successful
     * 			If False - Receive unsuccessful possibly try again or flag error.
     */
  	public boolean receiveDATA()
  	{	
  		timeoutFlag=false;
  		/* Encode the block number into the response block. */
  		byte[] blockArray = new byte[2];
  		blockArray[1]=(byte)(blockNum & 0xFF);
  		blockArray[0]=(byte)((blockNum >> 8)& 0xFF);
  		/* Receive Data. */
  		try {
  			sendReceiveSocket.receive(requestPacket);
  			retransmitACK=false;
  		} catch(SocketTimeoutException e){
  			//Retransmit every timeout
  			//Quit after 5 timeouts
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
  				startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data after retransmit (this is used for retransmits).
  				return true;
  			}
  			return false;

  		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
  		if (verbose)
  		{
  			console.print("Server: Checking DATA...");
  		}
  		byte[] data = requestPacket.getData();

		/* Check DATA address. */
  		if(!clientInet.equals(receivePacket.getAddress())){
  			buildError(5,receivePacket,"Invalid InetAddress");
  			console.print("Invalid InetAddress");
  			errorFlag=true;
			return false;
  		}
  		/* Check DATA port. */
  		if(receivePacket.getPort() != clientTID){
  			buildError(5,requestPacket,"Unexpected TID");
  			console.print("Unexpected TID");
  			errorFlag=true;
			return false;
  		}

  		/* Check DATA OpCode. */
  		if(data[0] == 0 && data[1] == 3){
			/* Check DATA length. */
			if(data.length > 516){
				buildError(4,receivePacket,"Length of the DATA is over 516.");
				errorFlag=true;
				return false;
			}
				
  			/* Check BlockNumber. */
  			if(blockArray[1] == data[3] && blockArray[0] == data[2]){
  				blockNum++;
  				timeouts=0;
  				retransmitACK=false;
  			}
  			/* Received Duplicate. */
  			else{
  				if (verbose)
  		  		{
  		  			console.print("Received Duplicate Packet: ");
  		  			printReceivedPacket(requestPacket);
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
  					startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data after retransmit (this is used for retransmits).
  					return true;
  				}
  				return false;
  			}
  		}
  		
  		/* Received Error, print & quit. */
  		else if(data[0] == 0 && data[1] == 5){
  			printError(receivePacket);
  			errorFlag=true;
			return false;
  		}
  		/* Received invalid opcode, send back error. */
  		else{
  			buildError(5,receivePacket,"OpCode is invalid");
  			errorFlag=true;
			return false;
  		}
  		return true;
  	}
  	
  	/* Used to exit threads main loops. */
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
