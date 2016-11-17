/**
*Class:             TrashFactory.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    17/11/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Loosely vomits back an array of garbage bytes.
*					Similar to most other source code I write.
* 
* 
*Update Log:		v1.0.0
*						- logic added
*						- apparently Java can only save primitive bytes as signed
*						
*/
package errorhelpers;


//import
import java.util.Random;


public class TrashFactory 
{
	//declaring local instance variables
	Random randomNumGen;
	
	
	//generic constructor
	public TrashFactory()
	{
		randomNumGen = new Random();
	}
	
	
	//run the factory, seize the means of production later
	public Byte[] produce(int quantity)
	{
		//generate an array of trash
		Byte[] trash = new Byte[quantity];
		for(int i=0; i<quantity; i++)
		{
			trash[i] = (byte)randomNumGen.nextInt(256);
		}
		
		return trash;
	}
	
	
	/*
	//for testing please ignore
	public static void main (String[] args) 
	{
		TrashFactory tf = new TrashFactory();
		Byte[] b = tf.produce(25);
		
		for(int i=0; i<25; i++)
		{
			System.out.println(i + ": " + ((int)b[i] & 0xFF));
		}
		
	}
	*/
}
