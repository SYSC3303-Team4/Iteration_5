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
import java.util.Arrays;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;

class TFTPWriteThread extends ServerThread
{
    /**
     * The text area where this thread's output will be displayed.
     */
    JTextArea transcript;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;
    private DatagramPacket receivePacket1;
    private int blockNumber = 0;
	private boolean verbose;
    private String threadNumber;
    private String path= "DEFAULT_TEST_WRITE";
    public static final byte[] response = {0, 4, 0, 0};
    private JTextArea fileChooserFrame;
	private File file;
	private JFileChooser fileChooser;
    
    

    public TFTPWriteThread(ThreadGroup group,JTextArea transcript, DatagramPacket receivePacketInfo,String thread, Boolean verboseMode) {
    	super(group,thread);
        this.transcript = transcript;
        receivePacket = receivePacketInfo;  
        threadNumber = thread;
        verbose = verboseMode;
        try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block    
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
        
        fileChooserFrame = new JTextArea(5,40);
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {//file is found
		    file = fileChooser.getSelectedFile();//get file name
		}
    }

    public void run() {
		   

		   //Parsing Data for filename and mode 
		   ByteArrayOutputStream filename = new ByteArrayOutputStream();
		   ByteArrayOutputStream mode = new ByteArrayOutputStream();
		   boolean change = false; 
		   for(int i = 2; i<receivePacket.getData().length;i++){
			   if(receivePacket.getData()[i]>=32){
				   if(change == false){
					   filename.write(receivePacket.getData()[i]);
				   }
				   else{
					   mode.write(receivePacket.getData()[i]);
				   }
			   }
			   if(receivePacket.getData()[i]!=0){
				   if(receivePacket.getData()[i+1] == 0){
					   change = true;
					   i++;
				   }
				}
		   }
		   printReceivedPacket(receivePacket, verbose);
		    /* Exit Gracefully if the stop is requested. */
	       if(isInterrupted()){exitGraceFully();return;}
	       if(verbose){
	    	   System.out.println("Request parsed for:");
	    	   System.out.println("	Filename: " + new String(filename.toByteArray(),
				   0,filename.toByteArray().length));
	    	   System.out.println("	Mode: " + new String(mode.toByteArray(),
				   0,mode.toByteArray().length) + "\n");
			}
    	
	       while(!isInterrupted()){


		   //Build and send the first ACK reply in format:
		   /*
		  2 bytes    2 bytes
		  -------------------
	   ACK   | 04    |   Block #  |
		  --------------------
		    */
		   if(blockNumber == 0){
			   sendPacket = new DatagramPacket(response, response.length,
			       receivePacket.getAddress(), receivePacket.getPort());

			   printSendPacket(sendPacket,verbose);
		       /*
		       // Send the datagram packet to the client via a new socket.
		       try {
			   // Construct a new datagram socket and bind it to any port
			   // on the local host machine. This socket will be used to
			   // send UDP Datagram packets.
			   sendSocket = new DatagramSocket();
		       } catch (SocketException se) {
			   se.printStackTrace();
			   System.exit(1);
		       }
		       */

		       try {
			   sendReceiveSocket.send(sendPacket);
		       } catch (IOException e) {
			   e.printStackTrace();
			   System.exit(1);
		       }
		        /* Exit Gracefully if the stop is requested. */
				if(isInterrupted()){exitGraceFully();}
				if(verbose){
		       System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
		       System.out.println();
				}
		   }

	       //Wait for next DATA datagram in format:
	       /*
		  2 bytes    2 bytes       n bytes
		  ---------------------------------
	   DATA  | 03    |   Block #  |    Data    |
		  ---------------------------------
		*/
		   byte[] rawData = new byte[516];
		   receivePacket1 = new DatagramPacket(rawData, rawData.length);
		   
		    /* Exit Gracefully if the stop is requested. */
			if(isInterrupted()){continue;}
	       System.out.println("Server: Waiting for packet.");
	       // Block until a datagram packet is received from receiveSocket.
	       try {
	    	   sendReceiveSocket.receive(receivePacket1);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	    	   System.exit(1);
	       }
	      
	       printReceivedPacket(receivePacket1,verbose);
		       byte[] data = new byte[receivePacket1.getLength()-4];

		       //Parse data from DATA packet
		       for(int i = 4; i < receivePacket1.getLength();i++){
		    	   data[i-4] = receivePacket1.getData()[i];
		       }
		       


		       //Write file to directory
		       TFTPWriter writer = new TFTPWriter();
		       if(file.exists() && !file.isDirectory()) { 
		    	   buildError(6,receivePacket,verbose);
				}
				
		       try {
					writer.write(data,file.getAbsolutePath());
				} catch (AccessDeniedException e1) {
					buildError(2,receivePacket,verbose);
					e1.printStackTrace();
					//exit
				} 
				catch(IOException e2){
					buildError(3,receivePacket,verbose);
					e2.printStackTrace();
					//exit
				}

		       if(data.length<512){
		    	   if(verbose){
		    	   System.out.println("Server: Final Data Block Received.");
		    	   System.out.println("Server: Sending last ACK");
		    	   //SET INTERRUPT TO EXIT LOOP
		    	   }
		       }

		       //Sending the ACK for previous DATA packet in format:
		       /*
			  2 bytes    2 bytes
			  -------------------
		   ACK   | 04    |   Block #  |
			  --------------------
			*/

			   response[2]=receivePacket1.getData()[2];
			   response[3]=receivePacket1.getData()[3];
			   blockNumber++;

			   
			   
		       sendPacket = new DatagramPacket(response, response.length,
					     receivePacket.getAddress(), receivePacket.getPort());
				/* Exit Gracefully if the stop is requested. */
			   if(isInterrupted()){continue;}
			   printSendPacket(sendPacket,verbose);

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
			 if(isInterrupted()){continue;}
			 if(verbose){
		       System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
		       System.out.println();
			 }
	       
	    }
	    exitGraceFully();
    }
    

}
