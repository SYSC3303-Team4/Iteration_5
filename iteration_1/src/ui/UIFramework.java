/**
*Class:             UIFramework.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    12/010/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Basic framework outlining how any UI implemented will function.
*					Allows for easy modification later if a replacement or subsisted
*					UI is made. Abstract class > interface everyday.
* 
* 
*Update Log:		v1.0.0
*						- null
*/
package ui;



interface UIFramework 
{	
	//generic method to print to screen
	public abstract void print(String printable);

	//generic method to take in user input
	public abstract void input(String in);
	
	//generic method to take in user input and display it as well
	public abstract void inputAndPrint(String in);
}
