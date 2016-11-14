/**
*Class:             Input.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    14/11/2016                                              
*Version:           1.1.0                                                      
*                                                                                   
*Purpose:           Store values associated with error-related inputs
* 
* 
*Update Log:		v1.1.0
*						- added new possible error modes
*						- print method updated
*						- numerical modes replaced with nice constants
*					v1.0.1
*						- added packetType
*						- added human readability to toStringFancy()
*					v1.0.0
*						- null
*/
package inputs;


public class Input 
{
	//declaring class-wise constants
	//error modes
	public static final int ERR_DELAY 		= 0;		//	delay a packet
	public static final int ERR_DUPLICATE 	= 1;		//	duplicate a packet
	public static final int ERR_LOSE 		= 2;		//	lose a packet
	public static final int ERR_MODE		= 3;		//	alter the mode ofRRQ or WRQ to invalid
	public static final int ERR_ADD_DATA	= 4;		//	make data in packet over limit of 512
	public static final int ERR_OPCODE		= 5;		//	alter a packet op-code to an in greater than 5
	public static final int ERR_TID			= 6;		//	alter a packets destination port/IP
	public static final int ERR_BLOCKNUM	= 7;		//	incorrectly change block number
	
	//packet types
	public static final int PACKET_RRQ		= 1;		//RRQ Packet
	public static final int PACKET_WRQ		= 2;		//WRQ Packet
	public static final int PACKET_DATA		= 3;		//DATA Packet
	public static final int PACKET_ACK		= 4;		//ACK Packet
	public static final int PACKET_ERR		= 5;		//ERROR Packet
	
	//declaring local instance variables
	private int blockNum;
	private int mode;
	private int delay;
	private int packetType;
	
	
	//generic constructor
	public Input(int mode, int packetType, int blockNum, int delay)
	{
		this.mode = mode;
		this.blockNum = blockNum;
		this.delay = delay;
		this.packetType = packetType;
	}
	
	
	//generic accessors
	public int getBlockNum()
	{
		return blockNum;
	}
	public int getMode()
	{
		return mode;
	}
	public int getDelay()
	{
		return delay;
	}
	public int getPacketType()
	{
		return packetType;
	}
	
	
	@Override
	//print as string
	public String toString()
	{
		return ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || Delay: " + delay);
	}
	
	
	//return a fancy string
	public String toFancyString()
	{
		String printable = "";
		
		switch(mode)
		{
			case(ERR_DELAY):
				printable = printable + "DELAY ";
				break;
			case(ERR_DUPLICATE):
				printable = printable + "DUPLICATE ";
				break;
			case(ERR_LOSE):
				printable = printable + "LOSE ";
				break;
			case(ERR_MODE):
				printable = printable + "ALTER MODE of ";
				break;
			case(ERR_ADD_DATA):
				printable = printable + "ADD DATA to ";
				break;
			case(ERR_OPCODE):
				printable = printable + "ALTER OPCODE for ";
				break;
			case(ERR_TID):
				printable = printable + "ALTER TID for ";
				break;
			case(ERR_BLOCKNUM):
				printable = printable + "ALTER BLOCKNUM for ";
				break;
			default:
				printable = printable + "!BAD MODE! ";
				break;
		}
		switch(packetType)
		{
			case(PACKET_RRQ):
				printable = printable + "RRQ packet";
				break;
			case(PACKET_WRQ):
				printable = printable + "WRQ packet ";
				break;
			case(PACKET_DATA):
				printable = printable + "DATA packet ";
				break;
			case(PACKET_ACK):
				printable = printable + "ACK packet ";
				break;
			default:
				printable = printable + "!BAD PT! ";
				break;
		}
		printable = printable + blockNum;
		if(mode == 0)
		{
			printable = printable + " for " + delay + "block nums";
		}
		
		return printable;
	}
}
