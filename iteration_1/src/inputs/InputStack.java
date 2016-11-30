/**
*Class:             InputStack.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    30/11/2016                                              
*Version:           2.1.1                                                      
*                                                                                   
*Purpose:           Sorted stack of input strings. Sorted in terms of block num.
*					Really more of a sorted list now but laissez faire ce qu'ils font.
* 
* 
*Update Log:		v2.1.1
*						- length() renamed size()
*						- packet type ERROR sorting patched
*					v2.1.0
*						- sorting hierarchy patched to include errors and rrq/wrq
*						 		\-->assumes RRQ or WRQ are first
*						 		 \-->assumes ERROR packet are last
*						- throw an error if more than one RRQ/WRQ
*						- throw an error if more than one ERROR packet
*						- robust error detection added
*						- prints clearer
*					v2.0.0
*						- sorting now occurs after entire stack is created
*						- sorting based on RRQ or WRQ
*						- clear method
*						- bug associated with block-number-tie for WRQ patched
*					v1.0.1
*						- added human readable thing
*					v1.0.0
*						- very basic implementation of framework v1.0.0
*/
package inputs;


import java.util.Collections;
//import stuff
import java.util.LinkedList;


public class InputStack 
{
	//Declaring local instance variables
	private LinkedList<Input> pseudoStack;
	private int length;
	private boolean RWRQPresent;
	private boolean ERRPresent;
	
	
	//generic constructor
	public InputStack()
	{
		pseudoStack = new LinkedList<Input>();
		length = 0;
		RWRQPresent = false;
		ERRPresent = false;
	}
	
	
	public int size()
	{
		return pseudoStack.size();
	}
	
	
	//clear the InputStack
	public void clear()
	{
		pseudoStack.clear();
		length = 0;
		RWRQPresent = false;
		ERRPresent = false;
	}
	
	
	//sort with DATA > ACK in case of blockNum tie
	public void sortRRQ()
	{
		resolveTies(false);
	}
	
	
	//sort with ACK > DATA in case of blockNum tie
	public void sortWRQ()
	{
		resolveTies(true);
	}
	
	
	//general case for sort-with-tie
	private void resolveTies(boolean WRQ)
	{
		//declaring local variables
		Input inputInList = null;
		Input nextInput = null;
		
		//iterate through stack except for end
		for (int i=0; i<length-1; i++)
		{
			//get input at index i
			inputInList = pseudoStack.get(i);
			nextInput = pseudoStack.get(i+1);

			//check if there is a tie case between i and i+1
			if(inputInList.getBlockNum() == nextInput.getBlockNum())
			{
				if(WRQ)
				{
					/*          i              i+1
			              inputInList      nextInput
			   		 ... ---> DATA --------> ACK --> ...
					*/
					if (inputInList.getPacketType() == 3 && nextInput.getPacketType() == 4)
					{
						Collections.swap(pseudoStack, i, i+1);
					}
				}
				else
				{
					/*          i              i+1
			               inputInList      nextInput
			   		  ... ---> ACK ----------> DATA --> ...
					*/
					if (inputInList.getPacketType() == 4 && nextInput.getPacketType() == 3)
					{
						Collections.swap(pseudoStack, i, i+1);
					}
				}
			}
		}
	}
	
	
	//push to stack with sort (tie conditions not guaranteed)
	public void push(int mode, int packetType, int blockNum, int extraInt, String extraStr) throws InputStackException
	{
		//create Input
		Input newInput = new Input(mode, packetType, blockNum, extraInt, extraStr);
		
		//adding a RRQ/WRQ
		if (newInput.getPacketType() == Input.PACKET_RRQ || newInput.getPacketType() == Input.PACKET_WRQ)
		{
			if(!RWRQPresent)
			{
				pseudoStack.addFirst(newInput);
				length++;
				RWRQPresent = true;
			}
			else
			{
				throw new InputStackException("Only one RRQ/WRQ type error can be simulated at a time");
			}
		}
		//if of type ERROR
		else if (newInput.getPacketType() == Input.PACKET_ERR)
		{
			if(!ERRPresent)
			{
				pseudoStack.addLast(newInput);
				//length++;
				ERRPresent = true;
			}
			else
			{
				throw new InputStackException("Only one ERROR type error can be simulated at a time");
			}
		}
		//nothing in stack, add to front
		else if (length == 0)
		{
			pseudoStack.addFirst(newInput);
			length++;
		}
		//something in else list, have to iterate through it
		else
		{
			//declaring Input to represent current Input in list
			Input inputInList;
			
			//iterate through list to determine where to put it 
			for(int i=0; i<length; i++)
			{
				//get current input at index i
				inputInList = pseudoStack.get(i);
				
				//if duplicate found during push
				if(newInput.getBlockNum() == inputInList.getBlockNum() && newInput.getPacketType() == inputInList.getPacketType())
				{
					throw new InputStackException("Simulating error on packet type ["+ newInput.getPacketType() + "], blocknum ["+ newInput.getBlockNum() + "] already exists");
				}
				//new input should come before input in list
				else if(newInput.getBlockNum() < inputInList.getBlockNum())
				{
					pseudoStack.add(i, newInput);
					length++;
					return;
				}
			}
			//add to end
			pseudoStack.addLast(newInput);
			length++;
		}
	}
	
	
	//peek top entry from stack
	public Input peek()
	{
		if (length != 0)
		{
			return pseudoStack.getFirst();
		}
		else
		{
			return null;
		}
	}
	
	
	//pop top entry from stack
	public Input pop()
	{
		if (pseudoStack.size() != 0)
		{
			length--;
			return pseudoStack.removeFirst();
		}
		else
		{
			return null;
		}
	}
	
	
	@Override
	//print as string
	public String toString()
	{
		//return string
		String printable = "";
		
		//make returnable object
		if(length == 0)
		{
			printable = "EMPTY";
		}
		else
		{
			for(int i=0; i<length; i++)
			{
				printable = printable + "ITEM: " + i + " --> " + pseudoStack.get(i).toString() + "\n";
			}
		}
		
		return printable;
	}
	
	
	//print as HUMAN READABLE string
	public String toFancyString()
	{
		//return string
		String printable = "";
		
		//make returnable object
		if(pseudoStack.size() == 0)
		{
			printable = "EMPTY";
		}
		else
		{
			for(int i=0; i<pseudoStack.size(); i++)
			{
				printable = printable + "ITEM: " + i + " --> " + pseudoStack.get(i).toFancyString() + "\n" + "    ";
			}
		}
		
		return printable;
	}
	
	
	/*
	//ONLY USED FOR TESTING
	public static void main(String[] args)
	{
		//(int mode, int packetType, int blockNum, int delay)
		
		InputStack inputStack = new InputStack();
		InputStack stack2 = new InputStack();
		
		System.out.println("Testing RRQ priority...");
		inputStack.push(2, 3, 1, 0);
		inputStack.push(2, 4, 1, 0);
		inputStack.push(0, 3, 3, 0);
		inputStack.push(1, 4, 2, 0);
		inputStack.push(1, 4, 3, 0);
		inputStack.push(0, 4, 5, 0);
		inputStack.push(0, 3, 5, 0);
		System.out.println(inputStack.toString());
		System.out.println("Sorting...");
		inputStack.sortRRQ();
		System.out.println(inputStack.toString());
		
		System.out.println("Testing WRQ priority...");
		stack2.push(2, 3, 1, 0);
		stack2.push(2, 4, 1, 0);
		stack2.push(0, 3, 3, 0);
		stack2.push(1, 4, 2, 0);
		stack2.push(1, 4, 3, 0);
		stack2.push(0, 3, 5, 0);
		stack2.push(0, 4, 5, 0);
		System.out.println(stack2.toString());
		System.out.println("Sorting...");
		stack2.sortWRQ();
		System.out.println(stack2.toString());
		System.out.println("\nTest Complete...");
	}
	*/
}
