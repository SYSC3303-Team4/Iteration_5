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
*Update Log:    	
*					v1.0.1
*						-changed Buffer to file output stream to make it work
*						-always set fileoutputstream bool to true, otherwise writes over file
*					v1.0.0
*                       - null
*/


//import stuff
import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;



public class TFTPWriter 
{
	private File file;
	
	public void write(byte[] data, String path) throws  IOException, AccessDeniedException, FileAlreadyExistsException 
	{
		
		
		file = new File(path);
		//prep to write to file
		FileOutputStream output = new FileOutputStream(file,true);//set to false if dont always want to write to end of file
		//write data to file starting at offset
		
		output.write(data);
		//output.write(data);
		//offset = offset + data.length;
		
		//close file
		output.close();
	}
	
	/*
	//test pls ignore
	public static void main( String args[] ) throws Exception
	{
		byte[] a,b,c;
		a = new byte[50];
		b = new byte[255];
		c = new byte[21];
		
		
	}
	*/

}
