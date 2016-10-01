/*
*Class:             TFTPWriter.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    29/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Blindly regurgitate arrays of bytes into a designated
*					file for storage. Will not overwrite a file unless specified to
*					(ie set offset to 0).
* 
* 
*Update Log:    	v1.0.0
*                       - null
*/


//import stuff
import java.io.*;



public class TFTPWriter 
{
	
	
	public void write(byte[] data, String file) throws FileNotFoundException, IOException 
	{
		//prep to write to file
		FileOutputStream output = new FileOutputStream(file,true);//set to false if dont always want to write to end of file
		//write data to file starting at offset
		
		output.write(data);
		//output.write(data);
		//offset = offset + data.length;
		
		//close file
		output.close();
	}

}
