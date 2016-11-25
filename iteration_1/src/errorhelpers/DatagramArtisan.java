/**
*Class:             DatagramArtisan.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    21/11/2016                                              
*Version:           1.1.0                                                      
*                                                                                   
*Purpose:           Homemade, artisan crafted datagrams.
*					Just like mom used to make and her mother before her.
*					Also can dissect and return specific parts in a packet.
* 
* 
*Update Log:		v1.1.0
*						- the artisan has expanded his talents, he now can provide detailed
*						  information from any packet
*					v1.0.0
*						- produceDATA added
*						- produceACK added
*						- produceRWRQ added
*						
*/
package errorhelpers;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class DatagramArtisan
{		
	//generic constructor
	public DatagramArtisan() {}
	
	
	//produce an ACK
	public DatagramPacket produceACK(byte[] opCode, int blockNum, InetAddress address, int outPort)
	{
		//ack skellington + local variables
		byte[] ack = new byte[4];
		byte[] blockNumArr = new byte[2];
		
		//add opcode to ACK
		ack[0] = opCode[0];
		ack[1] = opCode[1];
		
		//add block nums to ACK
		ack[2] = (byte)((blockNum >> 8)& 0xFF);
		ack[3] = (byte)(blockNum & 0xFF);
		
		//generate and save datagram packet
		return new DatagramPacket(ack, ack.length, address, outPort);
	}
	
	
	//produce a DATA
	public DatagramPacket produceDATA(byte[] opCode, int blockNum, byte[] data, InetAddress address,int outPort)
	{
		//prep for block num
		byte[] blockNumArr = new byte[2];
		blockNumArr[1]=(byte)(blockNum & 0xFF);
		blockNumArr[0]=(byte)((blockNum >> 8)& 0xFF);

		//construct array to hold data
		byte[] toSend = new byte[data.length + 4];
		
		//add opcode
		for(int i=0; i<2; i++)
		{
			toSend[i] = opCode[i];
		}
		//add blocknum
		for(int i = 2; i < 4; i++)
		{
			toSend[i] = blockNumArr[i-2] ;
		}
		//add data
		for(int i = 0; i < data.length; i++)
		{
			toSend[i+4] = data[i] ;
		}
		
		//generate and return
		return (new DatagramPacket(toSend, toSend.length, address, outPort));
	}
	
	
	//produce a RRQ
	public DatagramPacket produceRWRQ(byte[] opCode, String fileName, String mode, InetAddress address, int outPort)
	{
		//generate the data to be sent in datagram packet
		//convert various strings to Byte arrays
		byte[] fileNameBA = fileName.getBytes();
		byte[] modeBA = mode.getBytes();
		
		//compute length of data being sent (metadata include) and create byte array
		byte[] data = new byte[fileNameBA.length + modeBA.length + 4];
		int i = 2;
			
		//add first 2 bytes of opcode
		for(int c=0; c<2; c++)
		{
			data[c] = opCode[c] ;
		}
		//add text
		for(int c=0; c<fileNameBA.length; c++, i++)
		{
			data[i] = fileNameBA[c];
		}
		//add pesky 0x00
		data[i] = 0x00;
		i++;
		//add mode
		for(int c=0; c<modeBA.length; c++, i++)
		{
			data[i] = modeBA[c];
		}
		//add end metadata
		data[i] = 0x00;
			
		//generate and save datagram packet
		return new DatagramPacket(data, data.length, address, outPort);
	}
	
	
	//dissect and return packet type from any packet
	public byte getPacketType(DatagramPacket datagram)
	{
		return datagram.getData()[1];
	}
	
	
	//return the 2 byte opcode
	public byte[] getOpCode(DatagramPacket datagram)
	{
		byte[] data = datagram.getData();
		byte[] opCode = {data[0],data[1]};
		return opCode;
	}
	
	
	//return the filename from a RRQ or WRQ
	//returns null if not a valid packet type
	public String getFileName(DatagramPacket datagram)
	{
		byte[] rawData = datagram.getData();
		String fileName = "";
		
		//valid RRQ/WRQ, extract mode
		if(datagram.getData()[1] == 1 || datagram.getData()[1] == 2)
		{
			/*
			 * step backwards through RRQ/WRQ until you hit null
			 *     2B       Str     1B   Str   1B
			 *  ----------------------------------
			 * | OPCODE | FILENAME | 0 | MODE | 0 |
			 *  ----------------------------------
			 */
			for(int i=2; rawData[i] != 0x00; i++)
			{
				fileName = fileName + (char)rawData[i];
			}
			return fileName;
		}
		//invalid, no mode in packet, return null
		else
		{
			return null;
		}
	}
	
	
	//dissect and return mode from a RRQ or WRQ
	//returns null if not a valid packet type
	public String getMode(DatagramPacket datagram)
	{
		byte[] rawData = datagram.getData();
		String mode = "";
		
		//valid RRQ/WRQ, extract mode
		if(datagram.getData()[1] == 1 || datagram.getData()[1] == 2)
		{
			/*
			 * step backwards through RRQ/WRQ until you hit null
			 *     2B       Str     1B   Str   1B
			 *  ----------------------------------
			 * | OPCODE | FILENAME | 0 | MODE | 0 |
			 *  ----------------------------------
			 */
			for(int i=datagram.getLength()-2; rawData[i] != 0x00; i--)
			{
				mode = (char)rawData[i] + mode;
			}
			return mode;
		}
		//invalid, no mode in packet, return null
		else
		{
			return null;
		}
	}
	
	
	//return block number for ACK or DATA
	public int getBlockNum(DatagramPacket datagram)
	{
		byte[] rawData = datagram.getData();
		int blockNum = ( (rawData[2] << 8)&0xFF | rawData[3]&0xFF );
				
		return blockNum;
	}
	
	
	//return the data in a DATA packet
	public byte[] getData(DatagramPacket datagram)
	{
		byte[] rawData = datagram.getData();
		byte[] data = new byte[datagram.getLength()-4];
		
		for(int i=4; i<datagram.getLength(); i++)
		{
			data[i-4] = rawData[i];
		}
		
		return data;
	}
	
	
	
	//used for testing please do not delete
	public static void main (String[] args)
	{
		DatagramArtisan da = new DatagramArtisan();
		DatagramPacket packet = null;
		byte[] opCode = {(byte)0, (byte)2};
		byte[] data = new byte[20];
		for(int i=0; i<20; i++)
		{
			data[i] = (byte)i;
		}
		InetAddress localAddress = null;
		try
		{
			localAddress = InetAddress.getLocalHost();
		}
		catch(Exception e) {}
		
		//test produceRWRQ and reading info from RRQ/WRQ
		System.out.println("Testing produceRWRQ...\n============================");
		System.out.println("OpCode set: 2");
		System.out.println("FileName set: 'NeverForgetti'");
		System.out.println("Mode set: 'MomsSpaghetti'");
		System.out.println("Address set: " + localAddress.toString());
		System.out.println("Outport set: 8888\n");
		packet = da.produceRWRQ(opCode, "NeverForgetti", "MomsSpaghetti", localAddress, 8888);	
		System.out.println("Reading produced packet...\n============================");
		System.out.println("OpCode read: " + da.getPacketType(packet));
		System.out.println("FileName read: '" + da.getFileName(packet) + "'");
		System.out.println("Mode read: '" + da.getMode(packet) + "'");
		System.out.println("Address read: " + packet.getAddress());
		System.out.println("Outport read: " + packet.getPort());
		
		//test produceACK and reading relevant info
		opCode[0] = 0;
		opCode[1] = 4;
		packet = null;
		System.out.println("\nTesting produceACK...\n============================");
		System.out.println("OpCode set: 4");
		System.out.println("BlockNum set: 21");
		System.out.println("Address set: " + localAddress.toString());
		System.out.println("Outport set: 8888\n");
		packet = da.produceACK(opCode, 21, localAddress, 8888);
		System.out.println("Reading produced packet...\n============================");
		System.out.println("OpCode read: " + da.getPacketType(packet));
		System.out.println("BlockNum read: " + da.getBlockNum(packet));
		System.out.println("Address read: " + packet.getAddress());
		System.out.println("Outport read: " + packet.getPort());
		
		//test produceDATA and reading relevant info
		opCode[0] = 0;
		opCode[1] = 3;
		packet = null;
		System.out.println("\nTesting produceDATA...\n============================");
		System.out.println("OpCode set: 3");
		System.out.println("BlockNum set: 8");
		System.out.println("Data set: {1,2,3,4,...,19}");
		System.out.println("Address set: )" + localAddress.toString());
		System.out.println("Outport set: 69\n");
		packet = da.produceDATA(opCode, 8, data, localAddress, 69);
		System.out.println("Reading produced packet...\n============================");
		System.out.println("OpCode read: " + da.getPacketType(packet));
		System.out.println("BlockNum read: " + da.getBlockNum(packet));
		String temp = "";
		byte[] b = da.getData(packet);
		for(int i=0; i<b.length; i++)
		{
			temp=temp+b[i]+", ";
		}
		System.out.println("Data read: "+temp);
		System.out.println("Address read: " + packet.getAddress());
		System.out.println("Outport read: " + packet.getPort());
	}
}