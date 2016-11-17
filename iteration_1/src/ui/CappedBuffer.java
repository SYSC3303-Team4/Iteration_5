/**
*Class:             CappedBuffer.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    16/11/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Holds n amount of Strings. Navigation through this buffer is built in internally.
* 
* 
*Update Log:		v1.1.0
*						- looping through buffer removed
*					v1.0.0
*						- code for navigating through buffer added
*						- buffer loop logic implemented
*						- push logic implemented
*/
package ui;


//import external libraries
import java.util.LinkedList;


public class CappedBuffer 
{
	//declaring local instance variables
	private int maxSize;
	private int pos;
	private LinkedList<String> data;
	
	
	//generic constructor
	public CappedBuffer(int maxSize)
	{
		//initialize everything
		this.maxSize = maxSize;
		pos = 0;
		data = new LinkedList<String>();
	}
	
	
	//add to list
	public void push(String newString)
	{
		//remove last entry from list, add to head
		if (data.size() >= maxSize)
		{
			data.removeLast();
			data.addFirst(newString);
		}
		//list not full, add normally
		else
		{
			data.addFirst(newString);
		}
	}
	
	
	//return size of list
	public int getSize()
	{
		return data.size();
	}
	
	
	//return previous entry
	public String getOlder()
	{
		if(data.size() > 0)
		{
			//position is not at stackbase
			if (pos < data.size()-1)
			{
				pos++;
			}

			return data.get(pos);
		}
		else
		{
			return "";
		}
	}
	
	
	//return newer entry
	public String getNewer()
	{
		if(data.size() > 0)
		{
			//position is not at stacktop
			if (pos > 0)
			{
				pos--;
			}
			return data.get(pos);
		}
		else
		{
			return "";
		}
	}
	
	
	@Override
	//return whole buffer (used almost exclusively for debugging)
	public String toString()
	{
		String printable = "";
		if(data.size() > 0)
		{
			for(int i=0; i<data.size(); i++)
			{
				printable = printable + i + ": " + data.get(i) + "\n";
			}
		}
		else
		{
			printable = "empty";
		}
		return printable;
	}
}