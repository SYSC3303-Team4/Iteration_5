/**
*Class:             UIFramework.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    12/010/2016                                              
*Version:           1.0.1                                                   
*                                                                                   
*Purpose:           Basic framework outlining how any UI implemented will function.
*					Allows for easy modification later if a replacement or subsisted
*					UI is made.
* 
* 
*Update Log:		v2.0.0
*						- framework updated to reflect the more advanced needs of ConsoleUI.java
*					v1.0.0
*						- framework methods altered slightly
*/
package ui;



interface UIFramework 
{	
	//generic method to print to screen
	public abstract void print(String printable);
	
	//generic method to print indented to screen
	public abstract void printIndent(String printable);
	
	//generic method to print line to screen
	public abstract void println();

	//generic method to take in user input
	public abstract String getInput(boolean wait);
	
	//return PARSED input, set flag
	public abstract String[] getParsedInput(boolean wait);
	
	//print an array of bytes
	public abstract void printByteArray(byte[] b, int size);
	
	//generic method to clear screen
	public abstract void clear();
}
