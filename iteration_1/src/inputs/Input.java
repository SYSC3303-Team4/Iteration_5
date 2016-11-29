/**
*Class:             Input.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    15/11/2016                                              
*Version:           1.1.0                                                      
*                                                                                   
*Purpose:           Store values associated with error-related inputs
* 
* 
*Update Log:		v1.1.0
*						- added new possible error modes
*						- print method updated
*						- numerical modes replaced with nice constants
*						- constructor+accesors updated
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
	public static final int ERR_DELAY 		= 0;	//delay a packet
	public static final int ERR_DUPLICATE 	= 1;	//duplicate a packet
	public static final int ERR_LOSE 		= 2;	//lose a packet
	public static final int ERR_MODE		= 3;	//alter the mode of RRQ or WRQ to invalid
	public static final int ERR_ADD_DATA	= 4;	//make data in packet over limit of 512
	public static final int ERR_OPCODE		= 5;	//alter a packet opcode to an in greater than 5
	public static final int ERR_TID			= 6;	//alter a packets destination port
	public static final int ERR_BLOCKNUM	= 7;	//incorrectly change block number
	public static final int ERR_FORMAT		= 8;	//format incorrect
	//packet type
	public static final int PACKET_RRQ		= 1;	//RRQ Packet
	public static final int PACKET_WRQ		= 2;	//WRQ Packet
	public static final int PACKET_DATA		= 3;	//DATA Packet
	public static final int PACKET_ACK		= 4;	//ACK Packet
	public static final int PACKET_ERR		= 5;	//ERROR Packet
	
	//declaring local instance variables
	private int blockNum;
	private int mode;
	private int extraInt;
	private int packetType;
	private String extraStr;
	
	
	
	
	//generic constructor
	public Input(int mode, int packetType, int blockNum, int extraInt, String extraStr)
	{
		this.mode = mode;
		this.blockNum = blockNum;
		this.extraInt = extraInt;
		this.packetType = packetType;
		this.extraStr = extraStr;
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
	public int getPacketType()
	{
		return packetType;
	}
	//accessors for extraInt
	public int getDelay()
	{
		return extraInt;
	}
	public int getExtraBytes()
	{
		return extraInt;
	}
	public int getTID()
	{
		return extraInt;
	}
	public int getOpcode()
	{
		return extraInt;
	}
	public int getAlteredBlockNum()
	{
		return extraInt;
	}
	//accessors for extraStr
	public String getNewMode()
	{
		return extraStr;
	}
	
	
	
	@Override
	//print as string
	public String toString()
	{
		String printable = "";
		switch(mode)
		{
			case(ERR_DELAY):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || Delay: " + extraInt);
				break;
			case(ERR_DUPLICATE):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode);
				break;
			case(ERR_LOSE):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode);
				break;
			case(ERR_MODE):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode);
				break;
			case(ERR_ADD_DATA):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || New Mode: " + extraInt);
				break;
			case(ERR_OPCODE):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || Delay: " + extraInt);
				break;
			case(ERR_TID):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || TID: " + extraInt);
				break;
			case(ERR_BLOCKNUM):
				printable = ("BlockNum: " + blockNum + " || PacketType: " + packetType + " || Mode: " + mode + " || Alter BlockNum: " + extraInt);
				break;
			default:
				printable = ("Unknow Input Type");
				break;
		}
		return printable;
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
				printable = printable + "alter MODE of ";
				break;
			case(ERR_ADD_DATA):
				printable = printable + "ADD data to ";
				break;
			case(ERR_OPCODE):
				printable = printable + "alter OPCODE for ";
				break;
			case(ERR_TID):
				printable = printable + "alter TID for ";
				break;
			case(ERR_BLOCKNUM):
				printable = printable + "alter BLOCKNUM for ";
				break;
			case(ERR_FORMAT):
				printable = printable + "corrupt FORMAT for ";
				break;
			default:
				printable = printable + "!BAD MODE! ";
				break;
		}
		switch(packetType)
		{
			case(PACKET_RRQ):
				printable = printable + "RRQ packet ";
				break;
			case(PACKET_WRQ):
				printable = printable + "WRQ packet ";
				break;
			case(PACKET_DATA):
				printable = printable + "DATA packet " + blockNum +  " ";
				break;
			case(PACKET_ACK):
				printable = printable + "ACK packet " + blockNum +  " ";
				break;
			case(PACKET_ERR):
				printable = printable + "ERROR packet ";
				break;
			default:
				printable = printable + "!BAD PT! ";
				break;
		}
		switch(mode)
		{
			case(ERR_DELAY):
				printable = printable + "for" + extraInt + " sec";
				break;
			case(ERR_DUPLICATE):
				//do nothing, but not an error
				break;
			case(ERR_LOSE):
				//do nothing, but not an error
				break;
			case(ERR_MODE):
				printable = printable + "to '" + extraStr + "'";
				break;
			case(ERR_ADD_DATA):
				printable = printable + "(+" + extraInt + " bytes)";
				break;
			case(ERR_OPCODE):
				printable = printable + "to " + extraInt;
				break;
			case(ERR_TID):
				printable = printable + "to " + extraInt;
				break;
			case(ERR_BLOCKNUM):
				printable = printable + "to " + extraInt;
				break;
			case(ERR_FORMAT):
				//do nothing, but not an error
				break;
			default:
				printable = printable + "!BAD MODE! ";
				break;
		}
		return printable;
	}
}
