/**
*Class:             DatagramArtisan.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    21/11/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Homemade, artisan crafted datagrams.
*					Just like mom used to make.
* 
* 
*Update Log:		v1.0.0
*						- produceDATA added
*						- produceACK added
*						- produceRWRQ added
*						
*/
package errorhelpers;


import java.net.DatagramPacket;
import java.net.InetAddress;


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
		for(int i=0; i<2; i++)
		{
			ack[i] = opCode[i];
		}
		
		//add block nums to ACK
		blockNumArr[1]=(byte)(blockNum & 0xFF);
		blockNumArr[0]=(byte)((blockNum >> 8)& 0xFF);
		
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
}
