/**
*Class:             Console.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    08/11/2016                                              
*Version:           1.1.1                                                      
*                                                                                   
*Purpose:           Instead of launching 3 things, we launch 1 thing which launches 3 other things.
* 
* 
*Update Log:		v1.0.0
*						- I got sick of launching 3 things
*						- instantiated client, server, host in main on seperate threads
*/



       
public class TestBench 
{
	public static void main(String[] args) 
	{
		//prep server thread
		Thread threadA = new Thread()
				{
					public void run()
					{
						//this throws an exception I guess?????
						try
						{
							TFTPServer.main(null);
						}
						catch (Exception e)
						{
							//do absolutely nothing
						}
					}
				};
				
		//prep client thread
		Thread threadB = new Thread()
				{
					public void run()
					{
						TFTPClient.main(null);
					}
				};
		
		//prep host thread
		Thread threadC = new Thread()
				{
					public void run()
					{
						TFTPHost.main(null);
					}
				};
		
		//run all threads
		threadB.start();
		threadC.start();
		threadA.start();
		
	}

}
