// TFTPServer.java
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*; 
import java.net.*;

import javax.swing.JFileChooser;

import javax.swing.filechooser.FileNameExtensionFilter;

import ui.ConsoleUI;

public class TFTPServer implements ActionListener
{

	/* Types of requests we can receive. */
	public static enum Request { READ, WRITE, ERROR};

	/* UDP datagram packets and sockets used to send / receive. */
	private DatagramPacket receivePacket;
	private DatagramSocket receiveSocket, sendSocket;
	
	/* UI. */
	private static boolean verbose = false;
	private ConsoleUI console;
	private File file;
	private JFileChooser fileChooser;
    
    private boolean runFlag  = true;


	/**
	 * Build the Server UI and initialize sockets.
	 */

	public TFTPServer(String title)
	{
		/* Make and run the UI. */
		console = new ConsoleUI(title, this);
		console.run();
		console.colorScheme("halloween");
		
		try {
			/* Construct a datagram socket and bind it to port 69
			* on the local host machine. This socket will be used to
			* receive UDP Datagram packets.*/
			receiveSocket = new DatagramSocket(69);
			receiveSocket.setSoTimeout(5000);
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			console.print("SOCKET BIND ERROR");
			se.printStackTrace();
			System.exit(1);
		}
		/* Initialize Server Dump. */
		while(file == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Directories","*");
			fileChooser.setFileFilter(filter);
			fileChooser.setDialogTitle("Choose a directory to dump to on the server");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(fileChooser);
			if (result == JFileChooser.APPROVE_OPTION) {//file is found
				file = fileChooser.getSelectedFile();//get file name
			}
		}
	}
	
	/**
	 * Description: This method will wait for a request on port 69 and create 
	 * the corresponding request thread. 
	 * @throws Exception
	 */
	public void serverMain() throws Exception
	{
		byte[] data;

		Request req; // READ, WRITE or ERROR
		int len, j=0, k=0;
		int threadNum = 0;
		ThreadGroup initializedThreads = new ThreadGroup("ServerThread");
		
		/* Print starting text. */
		console.print("TFTPServer running");
		console.print("type 'help' for command list");
		console.print("~~~~~~~~~~~ COMMAND LIST ~~~~~~~~~~~");
		console.print("'help'                                   - print all commands and how to use them");
		console.print("'clear'                                  - clear screen");
		console.print("'close'                                 - exit client, close ports, be graceful");
		console.print("'verbose BOOL'                - toggle verbose mode as true or false");
		console.print("'test'                                    - runs a test for the console");
		console.print("'cd'                                      - set the directory you want server read/write from");
		console.print("'path'                                  - print the path the server will use");
		console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		console.println();
		
		//TODO DELETE THIS
		//==================================================
		TFTPServer.verbose = true;
		console.print("Verbose mode set " + verbose);
		//==================================================
		
		/* Main loop */
		while(runFlag) 
		{	
		   /* Loop until instructed to close.
			* Construct a DatagramPacket for receiving packets up
			* to 100 bytes long (the length of the byte array).*/

			data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

			console.print("Server: Listening for requests...");
			try {
				receiveSocket.receive(receivePacket);
			}
			catch(SocketTimeoutException e)
			{
				continue;// Timeout to see if the state of the runFlag has changed.
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			/* Print the received datagram. */
			console.print("Server: Packet received:");
			console.print("From host: " + receivePacket.getAddress());
			console.print("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			console.print("Length: " + len);

			console.printByteArray(data, len);
			console.printIndent("Cntn:  " + (new String(data,0,len)));

			String received = new String(data,0,len);// Form a String from the byte array.
			console.print(received);
			
			/* Process the received datagram. */
			
			// If it's a read, send back DATA (03) block 1
			// If it's a write, send back ACK (04) block 0
			// Otherwise, ignore it
			String fileName = "";
			String mode = "";
			if (data[0]==0 && data[1]==1) req = Request.READ; // Request is Read
			else if (data[0]==0 && data[1]==2) req = Request.WRITE; // Request is Write
			else req = Request.ERROR; // Request is Bad

			if (req!=Request.ERROR) { // check for filename
				/* Search for next all 0 byte. */
				for(j=2;j<len;j++) {
					if (data[j] == 0) break;
				}
				if (j==len) req=Request.ERROR; // didn't find a 0 byte
				if (j==2) req=Request.ERROR; // filename is 0 bytes long
				fileName = new String(data,2,j-2);
			}

			if(req!=Request.ERROR) { // check for mode
				/* Search for next all 0 byte. */
				for(k=j+1;k<len;k++) { 
					if (data[k] == 0) break;
				}
				if (k==len) req=Request.ERROR; // didn't find a 0 byte
				if (k==j+1) req=Request.ERROR; // mode is 0 bytes long
				mode = new String(data,j+1,k-j-1);
			}
			System.out.println(mode);
			if(k!=len-1) req=Request.ERROR; // other stuff at end of packet        

			/* Create a response. */
			if (req==Request.READ) { 
				console.print("Server: Generating Read Thread");
				threadNum++;
				Thread readRequest =  new TFTPReadThread(initializedThreads, receivePacket, "Thread "+threadNum, verbose,file, fileName,mode);
				readRequest.start();
			} else if (req==Request.WRITE) { 
				console.print("Server: Generating Write Thread");
				threadNum++;
				Thread writeRequest =  new TFTPWriteThread(initializedThreads, receivePacket,"Thread "+threadNum, verbose,file, fileName,mode);
				writeRequest.start();
			} else { // it was invalid, send 
				console.print("Server: Illegal Request");
	    		console.print("Illegal TFTP operation");
	    		String errorMsg = "Illegal TFTP operation.";
	        	data[0] = 0;
	        	data[1] = 5;
	        	data[2] = 0;
	        	data[3] = 4;
	        	for(int c = 0; c<errorMsg.length();c++){
	        		data[4+c] = errorMsg.getBytes()[c];
	        	}
	        	data[data.length-1] = 0;
	        	
	    	    DatagramPacket sendPacket = new DatagramPacket(data, data.length,
	    				     receivePacket.getAddress(), receivePacket.getPort());
	    	    console.print("Sending: Illegal TFTP operation Error Packet");

	    	       	try {
	    	       		sendSocket.send(sendPacket);
	    	       	} catch (IOException e) {
	    	       		e.printStackTrace();
	    	       		System.exit(1);
	    	       	}
				
			} 
		}
		console.print("Server Shut Down..");
	} 


	public static void main( String args[] ) throws Exception
	{
		TFTPServer c = new TFTPServer("TFTP Server");
		c.serverMain();
	}


	@Override
	/**
	 * ISR. Called whenever user has new input, interrupts current process *WHENEVER* new input
	 */
	public void actionPerformed(ActionEvent e) 
	{
		console.actionPerformed(e);//get input. Do not wait (in case ISR called prematurely we dont want to cause server lag)
		String[] input = console.getParsedInput(false);
		
		/* Process input, handle inputs based on param number. */
		if(input != null)
		{
			switch (input.length)
			{
				case(1):
					//print help
					if (input[0].equals("help"))
					{
						console.print("~~~~~~~~~~~ COMMAND LIST ~~~~~~~~~~~");
						console.print("'help'                                   - print all commands and how to use them");
						console.print("'clear'                                  - clear screen");
						console.print("'close'                                 - exit client, close ports, be graceful");
						console.print("'verbose BOOL'                - toggle verbose mode as true or false");
						console.print("'cd'                                      - set the directory you want server read/write from");
						console.print("'path'                                    - print the path the server will use");
						console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
						console.println();
					}
					//clear console
					else if (input[0].equals("clear"))
					{
						this.console.clear();
					}
					//print active directory
					else if (input[0].equals("path"))
					{
						console.print("Path set to: " + file.toString());
					}
					//change active directory
					else if (input[0].equals("cd"))
					{
						//get new path
						fileChooser = new JFileChooser();
						fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
						FileNameExtensionFilter filter = new FileNameExtensionFilter("Directories","*");
						fileChooser.setFileFilter(filter);
						fileChooser.setDialogTitle("Choose a directory to dump to on the server");
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int result = fileChooser.showOpenDialog(fileChooser);
						if (result == JFileChooser.APPROVE_OPTION) {//file is found
							file = fileChooser.getSelectedFile();//get file name
							
						if(verbose)
						{
							console.print("Path set to:"  + file.toString());
						}
					}
					}
					//close
					else if(input[0].equals("close"))
					{
						runFlag = false;
						if(verbose)
						{
							console.print("Beginning server shutdown...");
						}
					}
					//bad input
					else
					{
						console.print("! Unknown Input !");
					}
					break;
				
				case(2):
					//toggle verbose
					if(input[0].equals("verbose"))
					{
						if(input[1].equals("true"))
						{
							TFTPServer.verbose = true;
							console.print("Verbose mode set " + verbose);
						}
						else if (input[1].equals("false"))
						{
							TFTPServer.verbose = false;
							console.print("Verbose mode set " + verbose);
						}
						else
						{
							console.print("! Unknown Input !");
						}
					}
					//alter color scheme
					else if (input[0].equals("color") || input[0].equals("colour"))
					{
						boolean cs = console.colorScheme(input[1]);
						if (verbose)
						{
							if(cs)
							{
								console.print("color scheme set to: " + input[1]);
							}
							else
							{
								console.printSyntaxError("color scheme not found");
							}
						}
					}
					//bad input
					else
					{
						console.print("! Unknown Input !");
					}
					break;
					
				default:
					//bad input
					console.print("! Unknown Input !");
			}
		}
		/*we should never enter this. Implies ISR called prematurely or other class instances reading input
		before this instance */
		else
		{
			this.console.printError("ISR","ISR called prematurely");
		}
	}
}