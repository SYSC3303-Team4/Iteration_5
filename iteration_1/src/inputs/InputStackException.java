/**
*Class:             InputStackException.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    28/11/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Is throw from InputStack when a not allowed push occurs
* 
* 
*Update Log:		1.0.0
*						- null
*/
package inputs;

public class InputStackException extends Exception
{
	//generic constructor
	InputStackException(String msg)
	{
		super(msg);
	}
}
