/**
*Class:             TFTPHost.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    17/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Receives packet from Client, sends packet to Server and waits
					for Server response. Sends Server response back to Client. Repeats
					this process indefinitely. Designed to allow for the simulation of errors and lost packets in future.
* 
* 
*Update Log:        v2.0.0
*						- input methods added (non-isr)
*					v1.0.0
*                       - null
*/


//imports
import java.io.*;
import java.net.*;
import java.util.Stack;

import ui.ConsoleUI;


public class TFTPHost 
{
	
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket receivedPacket;
	private DatagramSocket inSocket;
	private DatagramSocket generalClientSocket;
	private DatagramSocket generalServerSocket;
	private int clientPort;
	private int serverThreadPort;
	private boolean verbose;
	private ConsoleUI console;
	private boolean runFlag;
	private Stack inputStack = new Stack();
		
	//declaring local class constants
	private static final int CLIENT_RECEIVE_PORT = 23;
	private static final int SERVER_RECEIVE_PORT = 69;
	private static final int MAX_SIZE = 512+4;
	private static final boolean LIT = true ; 			

	
	//generic constructor
	public TFTPHost()
	{
		//construct sockets
		try
		{
			inSocket = new DatagramSocket(CLIENT_RECEIVE_PORT);
			generalServerSocket = new DatagramSocket();
			generalClientSocket = new DatagramSocket();
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		//initialize echo --> off, run --> true
		verbose = false;
		runFlag = true;
		
		//run UI
		console = new ConsoleUI("Error Simulator");
		console.run();
	}
	
	
	//basic accessors and mutators
	public DatagramSocket getInSocket()
	{
		return inSocket;
	}
	public void setClientPort(int n)
	{
		clientPort = n;
	}
	public int getClientPort()
	{
		return clientPort;
	}
	public DatagramPacket getReceivedPacket()
	{
		return receivedPacket;
	}
	public void setVerbose(boolean f)
	{
		verbose = f;
	}
	
	
	//print datagram contents
	private void printDatagram(DatagramPacket datagram)
	{
		byte[] data = datagram.getData();
		int packetSize = datagram.getLength();

		console.printIndent("Source: " + datagram.getAddress());
		console.printIndent("Port:      " + datagram.getPort());
		console.printIndent("Bytes:   " + packetSize);
		console.printByteArray(data, packetSize);
		console.printIndent("Cntn:  " + (new String(data,0,packetSize)));
	}
	
	
	
	//receive packet on inPort
	public void receiveDatagram(DatagramSocket inputSocket)
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		receivedPacket = new DatagramPacket(arrayholder, arrayholder.length);
		
		//wait for incoming data
		console.print("Waiting for data...");
		try
		{
			inputSocket.receive(receivedPacket);
		}
		catch (IOException e)
		{
			System.out.print("Incoming socket timed out\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		//deconstruct packet and print contents
		console.print("Packet successfully received");
		printDatagram(receivedPacket);
	}
	
	
	//send packet to server and wait for server response
	/**
	 * 
	 */
	public void sendDatagram(int outPort, DatagramSocket socket)
	{
		//prep packet to send
		console.print("Sending packet...");
		sentPacket = receivedPacket;
		sentPacket.setPort(outPort );
		
		//print contents
		printDatagram(sentPacket);
		
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
	
	
	public void mainPassingLoop()
	{
		console.print("Console Operating...");
		
		//declaring local variables
		byte RWReq=0;
		boolean loop=true;
		int lastDataPacketLength=0;
		
		//print starting text
		console.print("TFTPClient running");
		console.print("type 'help' for command list");
		console.print("~~~~~~~~~~~ COMMAND LIST ~~~~~~~~~~~");
		console.print("'help'                                   - print all commands and how to use them");
		console.print("'clear'                                  - clear screen");
		console.print("'close'                                 - exit client, close ports, be graceful");
		console.print("'verbose BOOL'                - toggle verbose mode as true or false");
		console.print("'test'                                    - runs a test for the console");
		console.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		console.println();
		
		//main input loop
		while(runFlag && LIT)
		{
			
			
			
			
			
			
			
			
			
			
			
			
			
			/* OLD CODE
			//wait for original RRQ/WRQ from client
			receiveDatagram(inSocket);
			//save port 
			clientPort = receivedPacket.getPort();
		
			//determine if this is a valid RRQ/WRQ
			byte[] data = new byte[2];
			data = receivedPacket.getData();
			//valid
			if (data[0] == 0 && (data[1] == 1 || data[1] == 2) )
			{
				RWReq = data[1];
			}
			//something awful has happened
			else
			{
				console.print("Something awful happend");
				System.exit(0);
			}
			
			//send RRQ/WRQ to server
			sendDatagram(SERVER_RECEIVE_PORT, generalServerSocket);
			
			//receive 1st packet
			receiveDatagram(generalServerSocket);
			serverThreadPort = receivedPacket.getPort();
			
			
			//do the rest if RRQ
			if(RWReq == 1)
			{
				while(loop)
				{
					//save packet size if of type DATA
					if ( (receivedPacket.getData())[1] == 3)
					{
						lastDataPacketLength = receivedPacket.getLength();
					}
					//send DATA to client
					sendDatagram(clientPort, generalClientSocket);
					//receive client ACK
					receiveDatagram(generalClientSocket);
					//send ACK to server
					sendDatagram(serverThreadPort, generalServerSocket);
					
					//receive more data and loop if datagram.size==516
					//final ack sent to server, data transfer complete
					if (lastDataPacketLength < MAX_SIZE)
					{
						console.print("Data Transfer Complete");
						console.println();
						loop = false;
					}
					//more data left, receive and loop back
					else
					{
						receiveDatagram(generalServerSocket);
					}
				}
			}
			//do the rest if WRQ
			else
			{
				while(loop)
				{
					//send ACK to client
					sendDatagram(clientPort, generalClientSocket);
					//receive client DATA, save size
					receiveDatagram(generalClientSocket);
					if ( (receivedPacket.getData())[1] == 3)
					{
						lastDataPacketLength = receivedPacket.getLength();
					}
					//send DATA to server
					sendDatagram(serverThreadPort, generalServerSocket);
					
					//final DATA sent, receive and fwd final ACK
					if (lastDataPacketLength < MAX_SIZE)
					{
						//receive final server ACK
						receiveDatagram(generalServerSocket);
						//fwd ACK to clien
						sendDatagram(clientPort, generalClientSocket);
					
						//terminate transfer
						console.print("Data Transfer Complete");
						console.println();
						loop = false;
					}
					//there are still DATA packets to pass, transfer not compelte 
					else
					{
						//receive server ACK
						receiveDatagram(generalServerSocket);
					}
				}
			}
			*/	
		}
	}
	
	
	public static void main(String[] args) 
	{
		//declaring local variables
		TFTPHost host = new TFTPHost();
		//run
		host.mainPassingLoop();
	}

}
