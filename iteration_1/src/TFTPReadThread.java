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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;


import ui.ConsoleUI;

class TFTPReadThread  extends ServerThread
{
	//INIT general variables
	boolean sendZeroDataPacket = false;
	boolean duplicateACK = false;
	public final byte[] response = {0, 3, 0, 0};
	private File serverDump;

	public TFTPReadThread(ThreadGroup group, DatagramPacket requestPacketInfo, String thread, Boolean verboseMode,File path,String fileName, String mode) {
		super(group,thread,new ConsoleUI("Read Thread "+thread));
		console.run();
		
		requestPacket = requestPacketInfo;
		threadNumber  = thread;
		verbose = verboseMode; 
		serverDump = path;
		clientTID = requestPacketInfo.getPort();
		clientInet = requestPacketInfo.getAddress();
		this.fileName=fileName;
		this.mode=mode;
		
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			sendReceiveSocket.setSoTimeout(TIMEOUT*1000);
		} catch (SocketException e) {
			//Handle Timeout Exception
			e.printStackTrace();
			System.exit(0);
		}
	}


	public void run() {
		
		printReceivedPacket(requestPacket, verbose);
		if(verbose){
			console.print("Request parsed for:");
			console.print("	Filename: " + fileName + "\n");
			console.print("	Mode: " + mode + "\n");
		}

		/* Check for Valid MODE. */
		if(!mode.equalsIgnoreCase("netascii") && !mode.equalsIgnoreCase("octet")) {
			buildError(4,requestPacket,verbose,"Invalid Mode");
			exitGraceFully();
			return; 
		}

		/* Find File. */
		String absolutePath = serverDump.getAbsolutePath();
		File file = new File(absolutePath + "/" +fileName);
		
		/* File already exists error. */
		if(!file.exists())
		{
			buildError(1,requestPacket,verbose,"");
			return;
		}

		/* File permission error. */
		if(!file.canRead())
		{
			buildError(2,requestPacket,verbose,"");
			return;
		}
		
		/* Split the data into block in the reader . */
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
			return;
		}
		byte[] rawData = new byte[4];
		receivePacket = new DatagramPacket(rawData, rawData.length);
		receivePacket.setPort(clientTID);
		receivePacket.setAddress(clientInet);
		while(!stopRequested()){

			if(!retransmitDATA){
				/* Encode the block number into the response block. */
				response[3]=(byte)(blockNum & 0xFF);
				response[2]=(byte)((blockNum >> 8)& 0xFF);

				byte[] data = reader.pop();// Get the next blocks of data
				
				/* Check if the server needs to send a data Packet with 0 bytes. */
				if(data != null){
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
						startTime = System.currentTimeMillis();
						while(!receiveACK()){if(errorFlag){exitGraceFully();return;}}
						printReceivedPacket(finalACKPacket, verbose);
					}
					console.print("Read Request has completed.");
					requestStop();
					continue;
				}
				
				/* Builds the datagram in format
				 *
				 *		    2 bytes   2 bytes      n bytes 
				 *  		---------------------------------
				 *	 DATA  | 03    |   Block #  |    Data    |
				 *	    	---------------------------------
				 */

				byte dataPrime[] = Arrays.copyOf(response, response.length + data.length); 
				System.arraycopy(data, 0, dataPrime, response.length, data.length);

				sendPacket = new DatagramPacket(dataPrime, dataPrime.length,
						clientInet, clientTID);
			}
			printSendPacket(sendPacket, verbose);

			/* Send the datagram packet to the client via a new socket. */
			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1); 
			}
			
			if(verbose){
				console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"/n");
			}

			startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data (this is used for retransmits).
			while(!receiveACK()){if(errorFlag){exitGraceFully();return;}}//When this method returns it will either signal we have data, should retransmit or should exit with an error
			printReceivedPacket(requestPacket, verbose);


		}
		exitGraceFully();
	}


	private boolean stopRequested() {
		// TODO Auto-generated method stub
		return false;
	}

}