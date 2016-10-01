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
*/

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import javax.swing.JTextArea;

class TFTPWriteThread  extends ServerThread implements Runnable
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
    public static final byte[] response = {0, 4, 0, 0};
    
    

    public TFTPWriteThread(JTextArea transcript, DatagramPacket receivePacketInfo,String thread, Boolean verboseMode) {
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
		    
	       System.out.println("Server: Received packet:");
	       if(verbose){
	       System.out.println("From host: " + receivePacket.getAddress());
	       System.out.println("From host port: " + receivePacket.getPort());
	       System.out.println("Length: " + receivePacket.getLength());
	       System.out.println("Containing: ");
	       System.out.println(new String(receivePacket.getData(),0,receivePacket.getLength()));
	       }
		    /* Exit Gracefully if the stop is requested. */
			if(stopRequested){exitGraceFully();}
		   System.out.println("Request parsed for:");
		   System.out.println("	Filename: " + new String(filename.toByteArray(),
				   0,filename.toByteArray().length));
		   System.out.println("	Mode: " + new String(mode.toByteArray(),
				   0,mode.toByteArray().length) + "\n");
    	
    	while(true){
	       int len, j=0;



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
			   len = sendPacket.getLength();
			    /* Exit Gracefully if the stop is requested. */
		       if(stopRequested){exitGraceFully();}
		       System.out.println("Server: Sending packet:");
		       if(verbose){
		       System.out.println("To host: " + sendPacket.getAddress());
		       System.out.println("Destination host port: " + sendPacket.getPort());
		       System.out.println("Length: " + len);
		       System.out.println("Containing: ");
		       System.out.println(Arrays.toString(sendPacket.getData()));
		       }
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
				if(stopRequested){exitGraceFully();}
		       System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
		       System.out.println();
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
			if(stopRequested){exitGraceFully();}
	       System.out.println("Server: Waiting for packet.");
	       // Block until a datagram packet is received from receiveSocket.
	       try {
	    	   sendReceiveSocket.receive(receivePacket1);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	    	   System.exit(1);
	       }
	      
	       
	       

	       byte[] data = new byte[receivePacket1.getLength()-4];
	       //Parse data from DATA packet
	       for(int i = 4; i < receivePacket1.getLength();i++){
	    	   data[i-4] = receivePacket1.getData()[i];
	       }

	       
	       //Write file to directory
	       TFTPWriter writer = new TFTPWriter();
	       try {
				writer.write(data,"Tester" + filename.toString());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

	       if(data.length<512){
	    	   System.out.println("Server: Final Data Block Received.");
	    	   exitGraceFully();
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
		   if(stopRequested){exitGraceFully();}
	       System.out.println("Server: Sending packet:");
	       if(verbose){
	       System.out.println("To host: " + sendPacket.getAddress());
	       System.out.println("Destination host port: " + sendPacket.getPort());
	       len = sendPacket.getLength();
	       System.out.println("Length: " + len);
	       System.out.println("Block Number: " + blockNumber);
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
	       System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
	       System.out.println();
       }

       // We're finished with this socket, so close it.
       //sendReceiveSocket.close();
	
       //transcript.append(Thread.currentThread() + " finished\n");
    }
}
