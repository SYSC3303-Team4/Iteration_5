/**
 *Class:             TFTPWriterThread.java
 *Project:           TFTP Project - Group 4
 *Author:            Nathaniel Charlebois                                            
 *Date of Update:    29/09/2016                                              
 *Version:           1.0.0                                                      
 *                                                                                    
 *Purpose:           Handles the WRQs by:
 *						-Receiving the WRQ
 *						-Sending the special 0 ACK
 *						-Receiving the next DATA
 *						-Sending an ACK
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
 *                 	v1.1.2
 *                 		-Added Error handling
 *                 		-Added Error creating and sending
 *                 	v1.1.3
 *                 		-Corrected Error handling
 *                		-Fixed many a bug
 *                		-Refactored printing code 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import ui.ConsoleUI;

class TFTPWriteThread extends ServerThread
{
	public final byte[] response = {0, 4, 0, 0};

	//declaring local class constants
	private static final int ABSOLUTE_PACKET_BUFFER_SIZE = 1000;

	public TFTPWriteThread(ThreadGroup group, DatagramPacket requestPacketInfo,String thread, Boolean verboseMode,File serverDump,String fileName, String mode) {
		super(group,thread,new ConsoleUI("Write Thread "+thread));
		console.run();
		
		requestPacket = requestPacketInfo;  
		threadNumber = thread;
		verbose = verboseMode;
		clientTID = requestPacketInfo.getPort();
		clientInet = requestPacketInfo.getAddress();
		this.serverDump = serverDump; 
		this.fileName=fileName;
		this.mode=mode;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) { 
			e.printStackTrace();
			console.print(e.getMessage());
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
		TFTPWriter writer = new TFTPWriter();		
		/* Check for Valid MODE. */
		if(!(mode.equalsIgnoreCase("netascii") || mode.equalsIgnoreCase("octet"))) {
			buildError(4,requestPacket,"Invalid Mode");
			exitGraceFully();
			return; 
		}

		printReceivedPacket(requestPacket);
		if(verbose){
			console.print("Request parsed for:");
			console.print("	Filename: " + fileName);
			console.print("	Mode: " + mode + "\n");
		}

		/* Write file to directory. */
		File file = new File(serverDump.getAbsolutePath()+"/"+fileName);
		
		/* File already exists error. */
		if(file.exists()) { 
			buildError(6,requestPacket,"");
			return;
		}


		/* Build and send the first ACK reply in format: 
		*
		*           2 bytes    2 bytes
		*           -------------------
	   	*	ACK   | 04    |   Block #  |
		*           --------------------
		*
		*	NEVER RESENDS ACK 0
		*/
		sendPacket = new DatagramPacket(response, response.length,
				clientInet, clientTID);


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

		while(!stopRequested){
			/* Wait for next DATA datagram in format
			 *
			 * 		2 bytes    2 bytes       n bytes
			 *		---------------------------------
		   	 *DATA  | 03    |   Block #  |    Data    |
			 * 	    ---------------------------------
			 */
			byte[] rawData = new byte[ABSOLUTE_PACKET_BUFFER_SIZE]; 
			receivePacket = new DatagramPacket(rawData, rawData.length);//set up empty packet to receive into

			console.print("Server: Waiting for packet.");
			startTime = System.currentTimeMillis();//Set the start time that we attempt to receive data (this is used for retransmits).
			while(!receiveDATA()){if(errorFlag){exitGraceFully();return;}}//When this method returns it will either signal we have data, should retransmit or should exit with an error

			/* If you received valid data and do not wish to retransmit the last packet. */
			if(!retransmitACK){

				printReceivedPacket(receivePacket);
				byte[] data = new byte[receivePacket.getLength()-4];

				/* Parse data from DATA packet. */
				for(int i = 4; i < receivePacket.getLength();i++){
					data[i-4] = receivePacket.getData()[i];
				}
				
				/* Write the file. */
				try {
					writer.write(data,file.getAbsolutePath());
				} catch (SecurityException e1) {
					buildError(2,receivePacket,"");
					e1.printStackTrace();
					exitGraceFully();
					return;
				} 
				catch(IOException e2){
					buildError(3,receivePacket,"");
					exitGraceFully();
					return;
				}

				/* If the data is less than 512 then it signals the last packet. */
				if(data.length<512){
					if(verbose){
						console.print("Server: Final Data Block Received.");
						console.print("Server: Sending last ACK");
					}
					requestStop();
				}

				/* Send the first ACK reply in format: 
				*
				*           2 bytes    2 bytes
				*           -------------------
			   	*	ACK   | 04    |   Block #  |
				*           --------------------
				*/
				
				/* Set the block number of the response. */
				response[2]=receivePacket.getData()[2];
				response[3]=receivePacket.getData()[3];

			}

			sendPacket.setData(response);
			sendPacket.setLength(response.length);

			
			printSendPacket(sendPacket);

			/* Send response. */
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
		/* Exit Gracefully if the stop is requested. */
		exitGraceFully();
	}


}
