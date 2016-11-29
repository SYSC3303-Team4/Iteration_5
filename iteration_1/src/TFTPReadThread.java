/**
 *Class:             TFTPReadThread.java
 *Project:           TFTP Project - Group 4
 *Author:            Nathaniel Charlebois                                            
 *Date of Update:    29/09/2016                                              
 *Version:           1.1.4                                                      
 *                                                                                    
 *Purpose:           Handles the RRQs by:
 *						-Sending DATA
 *						-Receiving ACK
 *
 *To do:
 *	-Clean up code 
 *	-Test functionality with other components
 *	-Use the UI
 *
 *					
 *  
 *  
 *Update Log:    	v1.1.1
 *                       - null
 *                       
 *                  v1.1.2
 *                   	- Cleaned up code
 *                   	- Fixed Block numbers
 *                   
 *                 	v1.1.3
 *                 		-Corrected Error handling
 *                		-Fixed many a bug
 *                		-Refactored printing code
 *                
 *                 	v1.1.4
 *                 		-Added timeout/re-transmission protocol
 *                 		-
 *  
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.AccessDeniedException;
import java.util.*;

import javax.swing.JTextArea;

import ui.ConsoleUI;

class TFTPReadThread  extends ServerThread
{
	//INIT general variables
	boolean sendZeroDataPacket = false;
	boolean duplicateACK = false;
	private String threadNumber;
	public final byte[] response = {0, 3, 0, 0};
	private boolean terminate = false;
	private File serverDump;

	public TFTPReadThread(ThreadGroup group, DatagramPacket requestPacketInfo, String thread, Boolean verboseMode,File path) {
		super(group,thread,new ConsoleUI("Read Thread "+thread));
		console.run();
		requestPacket = requestPacketInfo;
		threadNumber  = thread;
		verbose = verboseMode; 
		serverDump = path;
		clientTID = requestPacketInfo.getPort();
		clientInet = requestPacketInfo.getAddress();
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block    
			e.printStackTrace();
		}
		try {
			sendReceiveSocket.setSoTimeout(TIMEOUT*1000);
		} catch (SocketException e) {
			//Handle Timeout Exception
			e.printStackTrace();
		}
	}

	public TFTPReadThread(ThreadGroup group,ConsoleUI transcript, DatagramPacket requestPacketInfo, String thread, Boolean verboseMode,File path) {
		super(group,thread,transcript);
		requestPacket = requestPacketInfo;
		threadNumber  = thread;
		verbose = verboseMode; 
		serverDump = path;
		clientTID = requestPacketInfo.getPort();
		clientInet = requestPacketInfo.getAddress();
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block    
			e.printStackTrace();
		}
		try {
			sendReceiveSocket.setSoTimeout(TIMEOUT*1000);
		} catch (SocketException e) {
			//Handle Timeout Exception
			e.printStackTrace();
		}
	}
	
	


	public void run() {

		connectionEstablished = true;
		
		printReceivedPacket(requestPacket, verbose);

		/* Exit Gracefully if the stop is requested. */
		if(isInterrupted()){exitGraceFully();return;}
		//Parsing Data for filename
		ByteArrayOutputStream filename = new ByteArrayOutputStream();
		ByteArrayOutputStream mode = new ByteArrayOutputStream();
		boolean change = false; 
		for(int i = 2; i<requestPacket.getData().length;i++){
			if(isInterrupted()){exitGraceFully();return;}
			if(requestPacket.getData()[i]>=32){
				if(change == false){
					filename.write(requestPacket.getData()[i]);
				}
				else{
					mode.write(requestPacket.getData()[i]);
				} 
			}
			if(requestPacket.getData()[i]!=0){
				if(requestPacket.getData()[i+1] == 0){
					change = true;//switch to parse mode
					i++;
				}
			}
		}


		/* Exit Gracefully if the stop is requested. */
		if(isInterrupted()){exitGraceFully();return;}
		if(verbose){
			console.print("Request parsed for:");
			console.print("	Filename: " + new String(filename.toByteArray(),0,filename.toByteArray().length));
			console.print("	Mode: " + new String(mode.toByteArray(),0,mode.toByteArray().length) + "\n");
		}
		
	   String modeString = new String(mode.toByteArray(), 
			   	0,mode.toByteArray().length);
	   //Check for Valid MODE
	   if((modeString.equalsIgnoreCase("netascii"))){

	   }
	   else if((modeString.equalsIgnoreCase("octet"))){

	   } else {
		   buildError(4,requestPacket,verbose,"Invalid Mode");
		   exitGraceFully();
    	   return; 
	   }

		String absolutePath = serverDump.getAbsolutePath();
		File file = new File(absolutePath + "/" +filename.toString());
		if(!file.exists())
		{
			buildError(1,requestPacket,verbose,"");
			return;
		}

		if(!file.canRead())
		{
			buildError(2,requestPacket,verbose,"");
			return;
		}
		TFTPReader reader = new TFTPReader();
		try {
			reader.readAndSplit(file.toString());

		} catch (FileNotFoundException e1) {
			if(file.exists())
			{
				buildError(2,requestPacket,verbose,"");
				return;
			}
			buildError(1,requestPacket,verbose,"");
			return;
		} catch (IOException e) {
			buildError(2,requestPacket,verbose,"");
			//e.printStackTrace();
			return;
		}
		byte[] rawData = new byte[4];
		int port = requestPacket.getPort();
		InetAddress address = requestPacket.getAddress();
		requestPacket = new DatagramPacket(rawData, rawData.length);
		requestPacket.setPort(port);
		requestPacket.setAddress(address);
		while(!stopRequested()){

			if(!retransmitDATA){
				//Encode the block number into the response block 
				response[3]=(byte)(blockNum & 0xFF);
				response[2]=(byte)((blockNum >> 8)& 0xFF);


				//Building datagram		


				byte[] data = reader.pop();
				//Check if the server needs to send a data Packet with 0 bytes
				if(data!=null){
					if(data.length==512 && reader.peek()==null){
						sendZeroDataPacket = true;
					}
				}

				/* If there's no more data to be read exit. */
				if(data == null){
					if(sendZeroDataPacket == true){
						sendNoData(requestPacket,verbose, blockNum,sendReceiveSocket);
						//Waiting to receive final ACK
						byte[] finalACK = new byte[4];
						DatagramPacket finalACKPacket = new DatagramPacket(finalACK, finalACK.length);
						try {
							sendReceiveSocket.receive(finalACKPacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						} 

						printReceivedPacket(finalACKPacket, verbose);
					}
					console.print("Read Request has completed.");
					exitGraceFully();
					return;
				}	
				//Builds the datagram in format
				/*
						2 bytes    2 bytes       n bytes 
						---------------------------------
					 DATA  | 03    |   Block #  |    Data    |
						---------------------------------
				 */

				byte dataPrime[] = Arrays.copyOf(response, response.length + data.length); 
				System.arraycopy(data, 0, dataPrime, response.length, data.length);

				/* Exit Gracefully if the stop is requested. */
				if(stopRequested()){continue;}
				sendPacket = new DatagramPacket(dataPrime, dataPrime.length,
						requestPacket.getAddress(), requestPacket.getPort());
			}
				printSendPacket(sendPacket, verbose);

				// Send the datagram packet to the client via a new socket.
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1); 
				}
				startTime = System.currentTimeMillis();
				/* Exit Gracefully if the stop is requested. */
				if(stopRequested()){continue;}
				if(verbose){
					console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"/n");
				}

			
			while(!receiveACK()){if(errorFlag){exitGraceFully();return;}}
			printReceivedPacket(requestPacket, verbose);
			

		}
		console.print("Server: thread closing.");
		exitGraceFully();
	}


	private boolean stopRequested() {
		// TODO Auto-generated method stub
		return false;
	}

}