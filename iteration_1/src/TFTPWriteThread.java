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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import ui.ConsoleUI;

class TFTPWriteThread extends ServerThread
{
    /**
     * The text area where this thread's output will be displayed.
     */
    private String threadNumber;
    File file;
    private boolean blockFlag=true;

    public static final byte[] response = {0, 4, 0, 0};
    
    private boolean initialFileCheck = false;
    
    

    public TFTPWriteThread(ThreadGroup group,ConsoleUI transcript, DatagramPacket requestPacketInfo,String thread, Boolean verboseMode,File file) {
    	super(group,thread,transcript);
    	requestPacket = requestPacketInfo;  
        threadNumber = thread;
        verbose = verboseMode;
        clientTID = requestPacketInfo.getPort();
        this.file = file;
        try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block    
			e.printStackTrace();
			console.print(e.getMessage());
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
		   //Parsing Data for filename and mode 
		   ByteArrayOutputStream filename = new ByteArrayOutputStream();
		   ByteArrayOutputStream mode = new ByteArrayOutputStream();
		   boolean change = false; 
		   for(int i = 2; i<requestPacket.getData().length;i++){
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
					   change = true;
					   i++;
				   }
				}
		   }
		   String modeString = new String(mode.toByteArray(), 
				   	0,mode.toByteArray().length);
		   
		   //Check for Valid MODE
		   if((modeString.equalsIgnoreCase("netascii"))){

		   }
		   else if((modeString.equalsIgnoreCase("octet"))){

		   } else {
			   buildError(4,requestPacket,verbose,"Invalid Mode");
	    	   return; 
		   }
		   
		   printReceivedPacket(requestPacket, verbose);
		    /* Exit Gracefully if the stop is requested. */
	       if(stopRequested){exitGraceFully();return;}  
	       if(verbose){
	    	   console.print("Request parsed for:");
	    	   console.print("	Filename: " + new String(filename.toByteArray(),
				   0,filename.toByteArray().length));
	    	   console.print("	Mode: " + new String(mode.toByteArray(),
				   0,mode.toByteArray().length) + "\n");
			}
		   //Build and send the first ACK reply in format:
		   /*
		  2 bytes    2 bytes
		  -------------------
	   ACK   | 04    |   Block #  |
		  --------------------
		    */
	       //NEVER RESENDS ACK 0
		   sendPacket = new DatagramPacket(response, response.length,
				   requestPacket.getAddress(), requestPacket.getPort());

		   printSendPacket(sendPacket,verbose);
		   
	       try {
	    	   sendReceiveSocket.send(sendPacket);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	    	   System.exit(1);
	       }
	       startTime = System.currentTimeMillis();
	       /* Exit Gracefully if the stop is requested. */
	       if(stopRequested){exitGraceFully();return;}
	       if(verbose){
	    	   console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"\n");
	       }

	       while(!stopRequested){
	    	   //Wait for next DATA datagram in format:
		       /*
			  2 bytes    2 bytes       n bytes
			  ---------------------------------
		   DATA  | 03    |   Block #  |    Data    |
			  ---------------------------------
			*/
			   byte[] rawData = new byte[516];
			   requestPacket = new DatagramPacket(rawData, rawData.length);
			   
			    /* Exit Gracefully if the stop is requested. */
			   if(stopRequested){continue;}
			   
		       console.print("Server: Waiting for packet.");
		       // Block until a datagram packet is received from receiveSocket.
		       while(!receiveDATA()){if(errorFlag){return;}}

		       if(!retransmitACK){
		       
		    	   printReceivedPacket(requestPacket,verbose);
			       byte[] data = new byte[requestPacket.getLength()-4];
	
			       //Parse data from DATA packet
			       for(int i = 4; i < requestPacket.getLength();i++){
			    	   data[i-4] = requestPacket.getData()[i];
			       }
			       
	
			       //Write file to directory
			       File fileName = new File(file.getAbsolutePath()+"/"+filename.toString());
			       
			       
			       TFTPWriter writer = new TFTPWriter();
			       
			       if(initialFileCheck){
				       if(fileName.exists()) { 
				    	   buildError(6,requestPacket,verbose,"");
				    	   return;
						}
			       }

			       /*
			       if(!fileName.canWrite())
			       {
			    	   buildError(2,requestPacket,verbose);
			    	   return;
			       }
			       */
					
			       try {
						writer.write(data,file.getAbsolutePath()+"/"+filename.toString());
						initialFileCheck = false;
					} catch (SecurityException e1) {
						buildError(2,requestPacket,verbose,"");
						e1.printStackTrace();
						return;
					} 
			       catch(FileNotFoundException e2)
			       {
			    	   buildError(1,requestPacket,verbose,"");
			    	   return;
			       }
			       catch(IOException e2){
						buildError(3,requestPacket,verbose,"");
						return;
					}
	
			       if(data.length<512){
			    	   if(verbose){
			    	   console.print("Server: Final Data Block Received.");
			    	   console.print("Server: Sending last ACK");
			    	   }
			    	    requestStop();
			       }
	
			       //Sending the ACK for previous DATA packet in format:
			       /*
				  2 bytes    2 bytes
				  -------------------
			   ACK   | 04    |   Block #  |
				  --------------------
				*/
	
				   response[2]=requestPacket.getData()[2];
				   response[3]=requestPacket.getData()[3];
	
		       }
				   
		       sendPacket = new DatagramPacket(response, response.length,
		    		   requestPacket.getAddress(), requestPacket.getPort());
		       
		       /* Exit Gracefully if the stop is requested. */
		       printSendPacket(sendPacket,verbose);

		       try {
		    	   sendReceiveSocket.send(sendPacket);
		       } catch (IOException e) {
		    	   e.printStackTrace();
		    	   System.exit(1);
		       }
		       /* Exit Gracefully if the stop is requested. */
		       if(stopRequested){continue;}
		       if(verbose){
		    	   console.print("Server: packet sent using port " + sendReceiveSocket.getLocalPort()+"\n");
		       }

	    }
	    console.print("Server: thread closing.");
	    exitGraceFully();
    }
    

}
