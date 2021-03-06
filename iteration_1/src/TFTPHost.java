
/**
*Class:             TFTPHost.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    02/12/2016                                              
*Version:           2.2.1                                                      
*                                                                                    
*Purpose:           Receives packet from Client, sends packet to Server and waits
*					for Server response. Sends Server response back to Client. Repeats
*					this process indefinitely. Designed to allow for the simulation of errors and lost packets in future.
*
*					Note that due to similarity, most inputs with 3 verbs could be combined into one large input that handles
*					all 3 verb inputs. However, in order to check that user noun input is valid, they are in separate if statements.
* 
* 
*Update Log:        v2.2.1
*						- now able to enter filename errors to be simulated
*					v2.2.0
*						- error input method revamped
*						- format error added
*					v2.1.1
*						- removed unnecessary accessors/mutators
*					v2.1.0
*						- added new inputs for error types
*						- reset method added for InputStack
*						- updated help menus to reflect new errors
*						- code smell reduced by implementing and using table of constants for packet types/error types
*						- additional checking on input (ie user cant ask to corrupt transmition mode on non RRQ/WRQ anymore)
*						- method introduced to handle conversions from user string input for packet type to int packet type
*					v2.0.0
*						- input methods added (non-isr)
*						- input now saves to InputStack
*						- help menu added
*					v1.0.0
*                       - null
*/


//imports
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import ui.ConsoleUI;
import inputs.*;
import errorhelpers.*;



public class TFTPHost implements KeyListener
{
	//declaring class-wise constants
	//error modes
	public final int ERR_DELAY 		= 0;	//delay a packet
	public final int ERR_DUPLICATE 	= 1;	//duplicate a packet
	public final int ERR_LOSE 		= 2;	//lose a packet
	public final int ERR_MODE		= 3;	//alter the mode of RRQ or WRQ to invalid
	public final int ERR_ADD_DATA	= 4;	//make data in packet over limit of 512
	public final int ERR_OPCODE		= 5;	//alter a packet opcode to an in greater than 5
	public final int ERR_TID		= 6;	//alter a packets destination port
	public final int ERR_BLOCKNUM	= 7;	//incorrectly change block number
	public final int ERR_FORMAT		= 8;	//format incorrect
	public final int ERR_FILENAME	= 9;	//mess up file name
	//packet type
	public final int PACKET_RRQ		= 1;	//RRQ Packet
	public final int PACKET_WRQ		= 2;	//WRQ Packet
	public final int PACKET_DATA	= 3;	//DATA Packet
	public final int PACKET_ACK		= 4;	//ACK Packet
	public final int PACKET_ERR		= 5;	//ERROR Packet
		
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket receivedPacket;
	private DatagramPacket lastReceivedPacket;//used in the delay packet, or order to know what to resend
	private DatagramSocket inSocket;//initial sending socket
	private DatagramSocket genSocket;
	
	
	private int clientPort;//client port to send to
	private boolean verbose;
	private ConsoleUI console;
	private InputStack inputStack = new InputStack();
	private DatagramArtisan dataArt=new DatagramArtisan();
	private boolean runFlag = true;
	private boolean haltRequested = false;
	private int resetCount = 0;
	
	private InetAddress serverIP=null; //IP for server computer, local by default
	private InetAddress clientIP=null;// IP for client computer, local by default
	private int serverPort=0;//Set to relevant value after first packet received from server
	private InetAddress sendToIP;//swap value for where to send packet
	    
	private boolean needSend=true;//used to flip between listening and sending
	
	//declaring local class constants
	private static final int CLIENT_RECEIVE_PORT = 23;
	private static final int SERVER_RECEIVE_PORT = 69;
	private static final int MAX_SIZE = 512+4;
	private static final boolean LIT = true ; 	
	private static final int CLIENT_SERVER_TIMEOUT = 5;
	private static final int MAX_DELAY_SEGMENTS = 10000;


	//generic constructor
	public TFTPHost()
	{
		//construct sockets
		try
		{
			inSocket = new DatagramSocket(CLIENT_RECEIVE_PORT);
			genSocket=new DatagramSocket();
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		//initialize echo --> false
		verbose = false;
		
		//run UI
		console = new ConsoleUI("Error Simulator", this);
		console.run();
		console.colorScheme("prettyinpink");
				
		//set default IP
		try
		{
			serverIP = InetAddress.getLocalHost();
		}
		catch (Exception e)
		{
			console.printError("UnknownHost","Cannot get local IPAddress");
		}
	}
	
	
	//print datagram contents
	private void printDatagram(DatagramPacket datagram)
	{
		byte[] data = datagram.getData();
		int packetSize = datagram.getLength();

		console.printIndent("Source: " + datagram.getAddress());
		console.printIndent("Port:      " + datagram.getPort());
		console.printIndent("Bytes:   " + packetSize);
		console.printIndent("To IP: "+ datagram.getAddress());
		console.printByteArray(data, packetSize);
		console.printIndent("Cntn:  " + (new String(data,0,packetSize)));
	}
	
	
	
	//receive packet on inPort
	public void receiveDatagram(DatagramSocket inputSocket)	
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		receivedPacket = new DatagramPacket(arrayholder, arrayholder.length);
		lastReceivedPacket=receivedPacket;
		
		//set delay
		try
		{
			inputSocket.setSoTimeout(0);
		}
		catch (SocketException ioe)
		{
			console.printError("Socket Exception","Cannot set socket timeout");
		}
		
		//wait for incoming data
		if(verbose)
		{
			console.print("Waiting for data...");
		}
		try
		{
			inputSocket.receive(receivedPacket);
		}
		catch (IOException e)
		{
			console.printError("IOEception","Incoming socket timed out");
		}
	}
	
	
	//receive packet on inPort
	public void receiveDatagramTimed(DatagramSocket inputSocket) throws IOException				//TODO
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		receivedPacket = new DatagramPacket(arrayholder, arrayholder.length);
		lastReceivedPacket=receivedPacket;
		
		//set delay
		try
		{
			inputSocket.setSoTimeout(500);
		}
		catch (SocketException ioe)
		{
			console.printError("Socket Exception","Cannot set socket timeout");
		}
		
		//wait for incoming data
		inputSocket.receive(receivedPacket);
	}
	
	
	public DatagramPacket receive(DatagramSocket inputSocket, int timeOut) throws IOException
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		DatagramPacket incommingPacket = new DatagramPacket(arrayholder, arrayholder.length);
		
		//set delay
		try
		{
			inputSocket.setSoTimeout(timeOut);
		}
		catch (SocketException ioe)
		{
			console.printError("Socket Exception","Cannot set socket timeout");
		}
		
		//wait for incoming data
		console.print("Waiting for data...");
		inputSocket.receive(incommingPacket);
		receivedPacket=incommingPacket;
		
		//deconstruct packet and print contents
		console.print("Packet successfully received");
		if (verbose)
		{
			printDatagram(incommingPacket);
		}
		
		return incommingPacket;

	}
	
	// try to receive data during delay loops
	public void tryReceive(DatagramSocket inputSocket,int timeOut) throws IOException
	{
		byte[] arrayholder = new byte[MAX_SIZE];
		DatagramPacket incommingPacket = new DatagramPacket(arrayholder, arrayholder.length);
		
		//set delay
		try
		{
			inputSocket.setSoTimeout(timeOut);
		}
		catch (SocketException ioe)
		{
			console.printError("Socket Excpetion","Cannot set socket timeout");
		}
		
		//wait for incoming data
		console.print("Waiting for data...");
		inputSocket.receive(incommingPacket);
		
		
		//deconstruct packet and print contents
		console.print("Packet successfully received");
		if (verbose)
		{
			printDatagram(incommingPacket);
		}
		
		//rest delay
		try
		{
			inputSocket.setSoTimeout(0);
		}
		catch (SocketException ioe)
		{
			console.printError("Socket Exception","Cannot set socket timeout");
		}
	}
	
	
	
	//send packet to server and wait for server response
	public void sendDatagram(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		//prep packet to send
		if(verbose)
		{	
			console.print("Sending packet...");
		}
		sentPacket = receivedPacket;
		sentPacket.setPort(outPort);
		sentPacket.setAddress(sIP);
		//print contents
		if(verbose)
		{
			printDatagram(sentPacket);
		}
		//send packet
		try
		{
			socket.send(sentPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		console.print("Packet successfully sent");
		
	}
	//change mode to anything
	public void changeMode(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		console.print("Change mode selected, changing mode to"+ inputStack.peek().getNewMode());
		
		//conver int to byte
		byte newOP[] = new byte[2];
		newOP[1] = (byte)(receivedPacket.getData()[1] & 0xFF);
		newOP[0] = (byte)((receivedPacket.getData()[0] >> 8)& 0xFF);
		
		//create new packet
		receivedPacket=dataArt.produceRWRQ(newOP,dataArt.getFileName(receivedPacket),inputStack.peek().getNewMode(), sIP, outPort);
		
		sendDatagram(outPort,socket,sIP);
		needSend=false;
	}
	
	
	
	//tack on garbage to the outgoing packet
	public void addData(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		console.print("Adding " + inputStack.peek().getExtraBytes() + " Bytes of garbage to datagram...");
		//generate trash and prep DatagramArtisan
		byte[] trash = (new TrashFactory()).produce(inputStack.peek().getExtraBytes());
		DatagramArtisan da = new DatagramArtisan();
		
		//extract parameters from receivedPacket
		byte[] opCode = da.getOpCode(receivedPacket);
		int blockNum = da.getBlockNum(receivedPacket);
		byte[] data = da.getData(receivedPacket);
		int packetPort = receivedPacket.getPort();
		
		//tack on garbage in dataWithTrash[]
		byte[] dataWithTrash = new byte[data.length+trash.length];
		int i=0;
		for (; i<data.length; i++)
		{
			dataWithTrash[i] = data[i];
		}
		for (int c=0; i<dataWithTrash.length; i++,c++)
		{
			dataWithTrash[i] = trash[c];
		}

		//generate datagram and send datagram
		receivedPacket = da.produceDATA(opCode, blockNum, dataWithTrash, sIP, packetPort);
		sendDatagram(outPort,socket,sIP);
		
		needSend=false;
	}
	
	//change OP code of packet
	public void changeType(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		console.print("Changeing Type to" +inputStack.peek().getOpcode());
		
		//convert int to byte 
		byte newOP[] = new byte[2];
		newOP[1] = (byte)(inputStack.peek().getOpcode());
		newOP[0] = (byte)(0);
		
		
		if(receivedPacket.getData()[1]==1 ||receivedPacket.getData()[1]==2)
		{
			receivedPacket=dataArt.produceRWRQ(newOP,dataArt.getFileName(receivedPacket),dataArt.getMode(receivedPacket), sIP, outPort);
			if(verbose)
			{
				console.print("change read/write rrq");
		
			}
		}
		
		else if (receivedPacket.getData()[1]==3)
		{
			if(verbose)
			{
				console.print("change data");
			}
			receivedPacket=dataArt.produceDATA(newOP, dataArt.getBlockNum(receivedPacket), dataArt.getData(receivedPacket), sIP,outPort);
		}
		
		else if (receivedPacket.getData()[1]==4)
		{
			if(verbose)
			{
				console.print("change ack");
			}
			receivedPacket=dataArt.produceACK(newOP, dataArt.getBlockNum(receivedPacket), sIP,outPort);
		}
		
		else 
		{
			console.print("Invalide Mode");
		}
		
		
		sendDatagram(outPort,socket,sIP);
		needSend=false;
		
	}
	
	
	//send to input port
	public void changePort(int outPort, DatagramSocket socket, InetAddress sIP)
	{
		console.print("Sending Data to"+ inputStack.peek().getTID());
		sendDatagram(inputStack.peek().getTID(),socket,sIP);
		needSend=false;
	}
	
	
	//change block number
	public void changeBlock(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		console.print("Change Block slected, changing block number to "+inputStack.peek().getAlteredBlockNum());
		
		//conver into to byte
		byte newOP[] = new byte[2];
		newOP[1] = (byte)(receivedPacket.getData()[1] & 0xFF);
		newOP[0] = (byte)((receivedPacket.getData()[0] >> 8)& 0xFF);
		
		
		int newBlock = (inputStack.peek().getAlteredBlockNum());
		
		
		if(receivedPacket.getData()[1]==3)
		{
			
			receivedPacket=dataArt.produceDATA(newOP, newBlock, dataArt.getData(receivedPacket), sIP,outPort);
		}
		
		else if (receivedPacket.getData()[1]==4)
		{
			receivedPacket=dataArt.produceACK(newOP, newBlock, sIP,outPort);
		}
		
		else 
		{
			console.print("Invalide Block");
		}
		
		
		sendDatagram(outPort,socket,sIP);
		needSend=false;
		
	}
	
	//determine how to mess with packet, if at all
	public void passIt(int mode,int delay,int clientPort,DatagramSocket genSocket, InetAddress sIP)
	{
		if(mode==0)//delay
		{
			if(verbose)
			{
				console.print("Delaying Packet");
			}
			delayPack(delay, clientPort, genSocket,sIP);
		}
		
		else if(mode==1)//duplicate
		{
			if(verbose)
			{
				console.print("Duplicate Packet");
			}
			duplicatePack(clientPort, genSocket,sIP);
		}
		
		else if (mode==2)//lose
		{
			if(verbose)
			{
				console.print("Lose Packet");
			}
			losePack( clientPort, genSocket,sIP);
		}
		
		else if (mode==3)//change mode
		{
			if(verbose)
			{
				console.print("Changing Mode");
			}
			changeMode( clientPort, genSocket,sIP);
		}
		
		else if (mode==4)//add random data
		{
			if(verbose)
			{
				console.print("Adding extra Data");
				
			}
			
			addData(clientPort,genSocket,sIP);
		}
		
		else if (mode==5)//change packet type
		{
			if(verbose)
			{
				console.print("Changing Packet Type");
			}
			changeType(clientPort, genSocket,sIP);
		}
		
		else if (mode==6)
		{
			if(verbose)
			{
				console.print("Changing Destination Port");
			}
			changePort(clientPort,genSocket,sIP);
		}
		
		else if (mode==7)
		{
			if(verbose)
			{
				console.print("Changing Block Number");
			}
			changeBlock(clientPort,genSocket,sIP);
		}
		else if (mode==8)
		{
			if(verbose)
			{
				console.print("Changing Format");
			}
			changeFormat(clientPort,genSocket,sIP);
		}
		else if (mode==9)
		{
			if(verbose)
			{
				console.print("Changing Filename");
			}
			changeFileName(clientPort,genSocket,sIP);
		}
		
		
		else
		{
			console.printError("Mode","INCORRECT MODE");
		}
	}
	
	//delay packet
	public void delayPack(int delay, int clientPort,DatagramSocket  genSocket, InetAddress sIP)
	{
		int[] delayArray = new int[MAX_DELAY_SEGMENTS];
		
		if(verbose)
		{
			console.print("IN DELAY PACKET "+delay);
		}
		
		//puts length of time <Timeout into an array, until contents adds up to delay
		for(int k = 0; delay != 0; k++)
		{
			if(delay < CLIENT_SERVER_TIMEOUT)
			{
				delayArray[k] = delay;
				delay = 0;
			}
			else	
			{
				delayArray[k] = CLIENT_SERVER_TIMEOUT;
				delay = delay - CLIENT_SERVER_TIMEOUT;
			}
		}
		
		//iterated throught delay array, poping out between each time out, to receive new data. if any
		for(int i = 0; i < delayArray.length; i++ )
		{
			if(delayArray[i]>0)
			{
			
				try
					{
						if(verbose)
						{
							console.print("Delaying packet unless other received"+ delayArray[i]);
						}
						tryReceive(genSocket, delayArray[i]);//receive something random	
						
						//Handling received data
						if(receivedPacket.getPort()==clientPort)
						{
							sendDatagram(serverPort, genSocket,serverIP);
							needSend=false;
						}
						
						else if(receivedPacket.getPort()==serverPort)
						{
							sendDatagram(clientPort, genSocket,clientIP);
							needSend=false;
						}
					}
					catch (SocketException see)
					{
						console.printError("SocketException", "SOTIMEOUT SET RETURN ERRROR: Add coherent comments");
						return;
					}
					catch (IOException ioe)//timeout, did not receive data, should send delayed packet
					{
						if(delayArray[i+1]==0)
						{
							if(verbose)
							{
								console.print("Delay Reached, Data sent");
							}
							sendDatagram(clientPort, genSocket,sIP);
							needSend=false;
						}
					}
				}
			}
		
		if(verbose)
		{
			console.print("End of delay pack logic");
		}
		return;
	}
	
	public void changeFileName(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		if (verbose)
		{
			console.print("Changing File Name");
		}
		byte[] trash =(new TrashFactory()).produce(10);
		String fileName=trash.toString();
		receivedPacket=dataArt.produceRWRQ(dataArt.getOpCode(receivedPacket),fileName,dataArt.getMode(receivedPacket), sIP, outPort);
		sendDatagram(outPort,socket,sIP);
	}
	
	public void changeFormat(int outPort, DatagramSocket socket,InetAddress sIP)
	{
		if (verbose)
		{
			console.print("Changing Format");
		}
		
		//byte[] fileName = dataArt.getFileName(receivedPacket).getBytes();
		//fileName.replace(42,0);
		
		String fileName=dataArt.getFileName(receivedPacket);
		fileName=fileName.replace(".", "\0");
		
		
		receivedPacket=dataArt.produceRWRQ(dataArt.getOpCode(receivedPacket),fileName,dataArt.getMode(receivedPacket), sIP, outPort);
		sendDatagram(outPort,socket,sIP);
	}
	
	public void duplicatePack( int cPort,DatagramSocket  genSocket, InetAddress sIP)
	{
		DatagramPacket storedAck = null;
		
		//send datagram to clientPort
		
		sendDatagram(cPort, genSocket, sIP);
		needSend=false;
		//wait for ACK
		try
		{
			storedAck = receive(genSocket, 0);
		}
		catch(IOException timeout)
		{
			console.printError("IOException","HOST TIMEOUT WAITING FOR CLIENT TO TRANSMIT");
			return;
		}
		
		//send duplicate
		if (verbose)
		{
			console.print("Sending duplicate to client...");
		}
		
		receivedPacket=lastReceivedPacket;
		if(receivedPacket.getPort()==clientPort)
		{
			sendDatagram(serverPort, genSocket,sIP);
		}
		
		else if(receivedPacket.getPort()==serverPort)
		{
			sendDatagram(clientPort, genSocket,sIP);
		}
		needSend=false;
		//wait for (lack of) responsee
		try
		{
			receive(genSocket, 50);
			sendDatagram(cPort, genSocket,sIP);
			needSend=false;
			return;
		}
		catch (IOException ioe)
		{
			if (verbose)
			{
				console.print("No response from client with duplicate!");
			}
			lastReceivedPacket = storedAck; 
		}
	}
	
	//lose Packet, dont need to actually do anything
	public void losePack( int clientPort,DatagramSocket  genSocket, InetAddress sIP)
	{
		if(verbose)
		{
			console.print("Data Lost");
		}
		needSend=false;
	}
	
	//determin
	public void maybeSend(int clientPort,DatagramSocket genSocket,DatagramPacket receivedPacket, InetAddress sIP )
	{    
		if(inputStack.peek()!=null)	
		{
			if(verbose)
			{
				console.print("looking for proper block");
			}
			byte byteBlockNum[]=new byte[2];
			int bNum=inputStack.peek().getBlockNum();
			int mode=inputStack.peek().getMode();
			int delay=(inputStack.peek().getDelay())*1000;
			int packType=inputStack.peek().getPacketType();
			
			//converting ints to byte[]
			byte bytePackType[] = new byte[2];
			
			bytePackType[1] = (byte)(packType & 0xFF);
			bytePackType[0] = (byte)((packType >> 8)& 0xFF);
			
			byteBlockNum[1] = (byte)(bNum & 0xFF);
			byteBlockNum[0] = (byte)((bNum >> 8)& 0xFF);
			
			//packet type and number match, need to alter packet
			if(bytePackType[1]==receivedPacket.getData()[1] && bytePackType[0] == receivedPacket.getData()[0] && byteBlockNum[1]==receivedPacket.getData()[3] && byteBlockNum[0]==receivedPacket.getData()[2])
			{
				//proper packet type and block num, mess with this one right here
				if(verbose)
				{
					console.print("Block Match");
				}
				passIt(mode, delay,clientPort, genSocket, sIP);
				//sendDatagram(clientPort, genSocket);
				inputStack.pop();
 
			}
			/* Can't check block numbers for requests when mode changing.*/
			else if((bytePackType[1]==receivedPacket.getData()[1] && bytePackType[0] == receivedPacket.getData()[0]) &&  bytePackType[0] == 0 && (bytePackType[1] == 1  || (bytePackType[1] == 2)))
			{
				//proper packet type and block num, mess with this one right here
				if(verbose)
				{
					console.print("Request Match");
				}
				passIt(mode, delay,clientPort, genSocket, sIP);
				//sendDatagram(clientPort, genSocket);
				inputStack.pop();
 
			}
			//regular running
			else
			{
				if(verbose)
				{
					console.print("Not Proper block, sending normally");
				}
				sendDatagram(clientPort, genSocket,sIP);
				
			}
		}
	
		else
		{
			sendDatagram(clientPort, genSocket,sIP);
		}
	}
	
	
	public void errorSimHandle()
	{
		int sendToPort=SERVER_RECEIVE_PORT;
		
		//wait for original RRQ/WRQ from client
		receiveDatagram(inSocket);
		console.print("First Packet Recieved");
		
		//sort InputStack accordingly
		if ( (receivedPacket.getData())[1] == 1 )
		{
			inputStack.sortRRQ();
			if(verbose)
			{
				console.print("RRQ detected");
				console.print(inputStack.toFancyString());
			}
		}
		else if ( (receivedPacket.getData())[1] == 2 )
		{
			inputStack.sortWRQ();
			if(verbose)
			{
				console.print("WRQ detected");
				console.print(inputStack.toFancyString());
			}
		}
		
		//save port 
		clientPort = receivedPacket.getPort();
		serverPort=0;
		sendToIP=serverIP;
		//CHANGE WITH INPUT
		try{
		clientIP=InetAddress.getLocalHost();
		//sendToIP=InetAddress.getLocalHost();
		}
		
		catch(Exception e){};
		
		//serverIP=clientIP;
	
		while (!haltRequested)
		{
			//waiting to receieve Packet
			if(!needSend)
			{
				//receive for 1/2 sec then check to see if host has been terminated via user command
				boolean timeout = true;
				while(!haltRequested && timeout)
				{
					try
					{
						receiveDatagramTimed(genSocket);
						//packet received, do not loop
						timeout = false;
					}
					catch (IOException ioe)
					{
						//no packet received, try again
						timeout = true;
					}
				}
				
				//Received packet came from client, send to server
				//exit
				if (!haltRequested)
				{
					if(serverPort==0)
					{
						serverPort=receivedPacket.getPort();
						console.print("ServerIP= "+ serverIP);
					}
					
					if(receivedPacket.getPort()==clientPort)
					{
						sendToPort=serverPort;
						sendToIP=serverIP;
						needSend=true;
					}
					
					else if(receivedPacket.getPort()==serverPort)
					{
						sendToPort=clientPort;
						sendToIP=clientIP;
						needSend=true;
					}
					
					else
					{
						try
						{
							genSocket.setSoTimeout(0);
						}
						catch (SocketException ioe)
						{
							console.printError("SocketException","Cannot set socket timeout");
						}
					}
				}
			}
			
			
			else if(needSend)
			{
				maybeSend(sendToPort,genSocket, receivedPacket,sendToIP);
				needSend=false;
			}
		}
		
		//reset host flags
		console.print("reseting console...");
		haltRequested = false;
		inputStack.clear();
		needSend = true;
		serverPort = 0;
		resetCount++;
		console.println();
		console.print("---------------------- RESET " + resetCount + "----------------------");
		console.println();
	}
	
	public void mainPassingLoop()
	{
		//declaring local variables
		String input[] = null;
		
		//print starting text
		console.print("TFTPHost running...");
		console.print("type 'help' for command list");
		console.print("~~~~~~~~~~~ COMMAND LIST ~~~~~~~~~~~");
		console.print("'help'                                   - print all commands and how to use them");
		console.print("'clear'                                  - clear screen");
		console.print("'close'                                 - exit client, close ports, be graceful");
		console.print("'verbose BOOL'                - toggle verbose mode as true or false");
		console.print("'verbose'                            - echo if verbose if true or false");
		console.print("'test'                                    - runs a test for the console");
		console.print("'errors'                               - display a summary of all errors to be simulated");
		console.print("'reset'                                 - reset the errors to be simulated");
		console.print("'run'                                   - finalize the number of errors to simulate & start host");
		console.print("'ip'                                    - display the IP address host will send to");
		console.print("'ip NAME/xxx.xxx.xxx.xxx'  - change the outgoing IP address to NAME/xxx.xxx.xxx.xxx");
		console.print("'ip local'                        - change the outgoing IP address to the local machine");
		console.println();
		console.print("'delay PT DL'                   - set a delay for packet type PT, block number BN for DL sec");
		console.print("'dup PT'                            - duplicate packety type PT, block number BN");
		console.print("'lose PT'                          - lose packet type PT, block number BN");
		console.print("'mode PT STRING'        - set the mode on either a RRQ or WRQ to STRING");					
		console.print("'add PT.BN NUM'           - add NUM bytes of garbage data to PT data, blocknum BN");
		console.print("'opcode PT OP'              - change packet type PT, number BN's opcode to OP");				
		console.print("'tid PT TID'                      - change packet PT block number BN's destination port to TID");
		console.print("'blocknum PT B2'         - change packet PT, block number BN's block number to B2");
		console.print("'format PT'                      - corrupt the format on RRQ/WRQ packet PT");
		console.print("'filename PT'                  - corrupt the filename on RRQ/WRQ packet PT");
		console.println();
		console.print("*enter PT.BN for any data/ack packets, BN is the block num (ie data.2 for data 2)");
		console.print("**press the ESC key to reset the host to start a new transfer");
		console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		console.print("You MUST enter run once all desired errors are entered in order to start the Simulator."); 
		console.print("Error Simulator is not ready for data if run is not entered");
		console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		console.println();
		
		//TODO DELETE THIS
		//==================================================
		this.verbose = true;
		console.print("Verbose mode set " + verbose);
		//==================================================
		
		//main input loop
		while(runFlag && LIT)
		{
			//get PARSED user input
			input = console.getParsedInput(true);
			
			//process input based on param number
			handleInput(input);
		}
	}
	
	
	//parse input based on number of params
	private void handleInput(String input[])
	{
		switch(input.length)
		{
			case(1):
				//print commands
				if (input[0].equals("help"))
				{
					console.print("~~~~~~~~~~~ COMMAND LIST ~~~~~~~~~~~");
					console.print("'help'                                   - print all commands and how to use them");
					console.print("'clear'                                  - clear screen");
					console.print("'close'                                 - exit client, close ports, be graceful");
					console.print("'verbose BOOL'                - toggle verbose mode as true or false");
					console.print("'verbose'                            - echo if verbose if true or false");
					console.print("'test'                                    - runs a test for the console");
					console.print("'errors'                               - display a summary of all errors to be simulated");
					console.print("'reset'                                 - reset the errors to be simulated");
					console.print("'run'                                   - finalize the number of errors to simulate & start host");
					console.print("'ip'                                    - display the IP address host will send to");
					console.print("'ip NAME/xxx.xxx.xxx.xxx'  - change the outgoing IP address to NAME/xxx.xxx.xxx.xxx");
					console.print("'ip local'                        - change the outgoing IP address to the local machine");
					console.println();
					console.print("'delay PT DL'                   - set a delay for packet type PT, block number BN for DL sec");
					console.print("'dup PT'                            - duplicate packety type PT, block number BN");
					console.print("'lose PT'                          - lose packet type PT, block number BN");
					console.print("'mode PT STRING'        - set the mode on either a RRQ or WRQ to STRING");					
					console.print("'add PT.BN NUM'           - add NUM bytes of garbage data to PT data, blocknum BN");
					console.print("'opcode PT OP'              - change packet type PT, number BN's opcode to OP");				
					console.print("'tid PT TID'                      - change packet PT block number BN's destination port to TID");
					console.print("'blocknum PT B2'         - change packet PT, block number BN's block number to B2");
					console.print("'format PT'                      - corrupt the format on RRQ/WRQ packet PT");
					console.print("'filename PT'                  - corrupt the filename on RRQ/WRQ packet PT");
					console.println();
					console.print("*enter PT.BN for any data/ack packets, BN is the block num (ie data.2 for data 2)");
					console.print("**press the ESC key to reset the host to start a new transfer");
					console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					console.println();
				}
				//display inputs
				else if (input[0].equals("errors"))
				{
					console.print(inputStack.toFancyString());
				}
				//run the console
				else if (input[0].equals("run"))
				{
					errorSimHandle();
				}
				//clear console
				else if (input[0].equals("clear"))
				{
					console.clear();
				}
				//show ipOutAddress
				else if (input[0].equals("ip"))
				{
					console.print("ip Address: " + serverIP.toString());
				}
				//reset inputStack
				else if (input[0].equals("reset"))
				{
					inputStack.clear();
					if(verbose)
					{
						console.print("Errors to Simulate: " + inputStack.size());
					}
				}
				//close console with grace
				else if (input[0].equals("close"))
				{
					console.print("Closing with grace....");
					runFlag = false;
					//this.close();
					System.exit(0);
				}
				//run simple console test
				else if (input[0].equals("test"))
				{
					console.testAll();
				}
				//BAD INPUT
				else
				{
					console.print("Command not recognized");
				}
				break;
				
			case(2):
				//toggle verbose
				if (input[0].equals("verbose"))
				{
					if (input[1].equals("true"))
					{
						verbose = true;
						console.print("Verbose set true");
					}
					else if (input[1].equals("false"))
					{
						verbose = false;
					}
					else
					{
						console.print("Command not recognized");
					}
				}
			
				//change default sendToIP
				else if (input[0].equals("ip"))
				{
					if(input[1].equals("local"))
					{
						try
						{
							serverIP = InetAddress.getLocalHost();
						}
						catch(Exception e)
						{
							console.printError("UnknownHost","Cannot asertain local InetAddress");
						}
					}
					else
					{
						try
						{
							//declaring temporary method variables
							String host;
							byte[] addr;
							String seperateAtSlash[];
							String subStrBytes[];
							
							//parse host
							seperateAtSlash = input[1].split("/");
							host = seperateAtSlash[0];
							
							//parse addr
							subStrBytes = (seperateAtSlash[1]).split("\\.");
							
							addr = new byte[subStrBytes.length];
							for(int i=0; i<subStrBytes.length; i++)
							{
								addr[i] = (byte)Integer.parseInt(subStrBytes[i]);
							}
							
							//try to set ip
							serverIP = InetAddress.getByAddress(host, addr);
						}
						catch (Exception e)
						{
							console.printSyntaxError("Invalid IPAddress - must be of form 'host/xxx.xxx.xxx.xxx'");
						}

					}
				}
			
				//duplicate packet
				else if (input[0].equals("dup") || input[0].equals("" + this.ERR_DUPLICATE))
				{
					handleModePt(ERR_DUPLICATE, input);
				}
			
				//lose packet
				else if (input[0].equals("lose") || input[0].equals("" + this.ERR_LOSE))
				{
					handleModePt(ERR_LOSE, input);
				}
			
				//corrupt packet format
				else if (input[0].equals("format") || input[0].equals("" + this.ERR_FORMAT))
				{
					int pt;
					//get packet type
					try
					{
						pt = PTStringToInt(input[1]);
					}
					catch (NumberFormatException nfe)
					{
						console.printSyntaxError("Unrecognized packet type (parameter 1)");
						return;
					}
					
					//check if packet is of type RRQ or WRQ
					if (pt == this.PACKET_RRQ || pt == this.PACKET_WRQ)
					{
						handleModePt(ERR_FILENAME, input);
					}
					else
					{
						console.printSyntaxError("Packet must be of type rrq/wrq (parameter 1)");
					}
				}
			
				//corrupt filename
				else if (input[0].equals("filename") || input[0].equals("" + this.ERR_FILENAME))
				{
					int pt;
					//get packet type
					try
					{
						pt = PTStringToInt(input[1]);
					}
					catch (NumberFormatException nfe)
					{
						console.printSyntaxError("Unrecognized packet type (parameter 1)");
						return;
					}
					
					//check if packet is of type RRQ or WRQ
					if (pt == this.PACKET_RRQ || pt == this.PACKET_WRQ)
					{
						handleModePt(ERR_FILENAME, input);
					}
					else
					{
						console.printSyntaxError("Packet must be of type rrq/wrq (parameter 1)");
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
				//BAD INPUT
				else
				{
					console.print("Command not recognized");
				}
				break;
		
			case(3):
				//delay a packet
				if (input[0].equals("delay") || input[0].equals("" + this.ERR_DELAY))
				{
					handleModePtInt(ERR_DELAY, input);
				}
			
				//add garbage bytes to a data packet
				else if (input[0].equals("add") || input[0].equals("" + this.ERR_ADD_DATA))
				{
					int pt;
					//get packet type
					try
					{
						pt = PTStringToInt(input[1]);
					}
					catch (NumberFormatException nfe)
					{
						console.printSyntaxError("Unrecognized packet type (parameter 1)");
						return;
					}
					
					//check if packet is of type data
					if (pt == this.PACKET_DATA)
					{
						handleModePtInt(ERR_ADD_DATA, input);
					}
					else
					{
						console.printSyntaxError("Packet must be of type data (parameter 1)");
					}
				}
			
				//mess around with opcode
				else if (input[0].equals("opcode") || input[0].equals("" + this.ERR_OPCODE))
				{
					handleModePtInt(ERR_OPCODE, input);
				}
			
				//alter the tid
				else if (input[0].equals("tid") || input[0].equals("" + this.ERR_TID))
				{
					handleModePtInt(ERR_TID, input);
				}
			
				//alter the block num of a DATA or ACK
				else if (input[0].equals("blocknum") || input[0].equals("" + this.ERR_BLOCKNUM))
				{
					int pt;
					//get packet type
					try
					{
						pt = PTStringToInt(input[1]);
					}
					catch (NumberFormatException nfe)
					{
						console.printSyntaxError("Unrecognized packet type (parameter 1)");
						return;
					}
					
					//check if packet is of type data
					if (pt == this.PACKET_DATA || pt == this.PACKET_ACK)
					{
						handleModePtInt(ERR_BLOCKNUM, input);
					}
					else
					{
						console.printSyntaxError("Packet must be of type data (parameter 1)");
					}
				}
			
				//change mode of RRQ or WRQ
				else if (input[0].equals("mode") || input[0].equals("" + this.ERR_MODE))
				{
					//get packet type
					int pt = PTStringToInt(input[1]);
					
					//check that packet type is WRQ or RRQ
					if (pt == this.PACKET_RRQ || pt == this.PACKET_WRQ)
					{
						//add to stack
						try
						{
							inputStack.push(ERR_MODE, pt, 0, 0, input[2]);
						}
						catch (InputStackException ise)
						{
							console.printError("Input",ise.getMessage());
						}
					}
					else
					{
						console.printSyntaxError("Packet must be of type rrq/wrq (parameter 1)");
					}
				}
				
				//BAD INPUT
				else
				{
					console.print("Command not recognized");
				}
				break;
				
			case(4):
				break;
			default:
				console.print("Command not recognized");
				break;
		}
	}
	
	
	//generic input of form MODE PT
	private void handleModePt(int errorType, String input[])
	{
		int packetType = 0;
		int blockNum = 0;
		
		//get packet type
		try
		{
			packetType = PTStringToInt(input[1]);
		}
		catch (NumberFormatException nfe)
		{
			console.printSyntaxError("Unrecognized packet type (parameter 1)");
			return;
		}
		
		//rrq/wrq/error type set blocknum 0
		if(packetType == this.PACKET_RRQ || packetType == this.PACKET_WRQ || packetType == this.PACKET_ERR)
		{
			blockNum = 0;
		}
		//parse out blocknum
		else if (packetType == this.PACKET_DATA || packetType == this.PACKET_ACK)
		{
			//get block number
			try
			{
				//get blockNum substring
				int dotPos = input[1].indexOf('.');
				String substr = input[1].substring(dotPos+1, input[1].length());
				
				//convert to int
				blockNum = Integer.parseInt(substr);
			}
			catch (NumberFormatException nfe)
			{
				console.printSyntaxError("NaN (parameter 1)");
				return;
			}
		}
		
		//add to stack
		try
		{
			inputStack.push(errorType, packetType, blockNum, 0, null);
		}
		catch (InputStackException ise)
		{
			console.printError("Input",ise.getMessage());
		}
	}
	
	
	//generic input of form MODE PT INT
	private void handleModePtInt(int errorType, String input[])
	{
		int packetType = 0;
		int blockNum = 0;
		int extraInt = 0;
		
		//get packet type
		try
		{
			packetType = PTStringToInt(input[1]);
		}
		catch (NumberFormatException nfe)
		{
			console.printSyntaxError("Unrecognized packet type (parameter 1)");
			return;
		}
		
		//delay a rrq/wrq/error type
		if(packetType == this.PACKET_RRQ || packetType == this.PACKET_WRQ || packetType == this.PACKET_ERR)
		{
			//get delay quantity
			try
			{
				extraInt = Integer.parseInt(input[2]);
			}
			catch (NumberFormatException nfe)
			{
				console.printSyntaxError("NaN (parameter 2)");
				return;
			}
			blockNum = 0;
		}
		//delay a data/ack
		else if (packetType == this.PACKET_DATA || packetType == this.PACKET_ACK)
		{
			//get block number
			try
			{
				//get blockNum substring
				int dotPos = input[1].indexOf('.');
				String substr = input[1].substring(dotPos+1, input[1].length());
				
				//convert to int
				blockNum = Integer.parseInt(substr);
			}
			catch (NumberFormatException nfe)
			{
				console.printSyntaxError("NaN (parameter 1)");
				return;
			}
			
			//get delay quantity
			try
			{
				extraInt = Integer.parseInt(input[2]);
			}
			catch (NumberFormatException nfe)
			{
				console.printSyntaxError("NaN (parameter 2)");
				return;
			}
		}
		
		//add to stack
		try
		{
			inputStack.push(errorType, packetType, blockNum, extraInt, null);
		}
		catch (InputStackException ise)
		{
			console.printError("Input",ise.getMessage());
		}
	}
	

	//return numerical packet type based on string input for packet type
	private int PTStringToInt(String input) throws NumberFormatException
	{
		//remove the possible dot
		int dotPos = input.indexOf('.');
		if(dotPos > -1)
		{
			input = input.substring(0,dotPos);
		}
		
		if (input.equals("rrq"))
		{
			return this.PACKET_RRQ;
		}
		else if (input.equals("wrq"))
		{
			return this.PACKET_WRQ;
		}
		else if (input.equals("data"))
		{
			return this.PACKET_DATA;
		}
		else if (input.equals("ack"))
		{
			return this.PACKET_ACK;
		}
		else if (input.equals("error"))
		{
			return this.PACKET_ERR;
		}
		else
		{
			return Integer.parseInt(input);
		}
	}
	
	
	public static void main(String[] args) 
	{
		//declaring local variables
		TFTPHost host = new TFTPHost();
		//run
		host.mainPassingLoop();
	}


	@Override
	//tab pressed
	public void keyPressed(KeyEvent e) 
	{
		//declaring local method variables
	    int keyCode = e.getKeyCode();
	    
	    //keycode handler
	    switch(keyCode)
	    {
	    	//ESC key pressed
	    	case (KeyEvent.VK_ESCAPE):
	    		console.print("Requesting reset...");
	    		haltRequested = true;
	    		break;
	    }
	}


	@Override
	public void keyReleased(KeyEvent arg0) {}


	@Override
	public void keyTyped(KeyEvent arg0) {}

}
