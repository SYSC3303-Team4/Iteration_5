*********************************************************
*	______ _____  ___ ______  ___  ___ _____ 	*
*	| ___ \  ___|/ _ \|  _  \ |  \/  ||  ___|	*
*	| |_/ / |__ / /_\ \ | | | | .  . || |__  	*
*	|    /|  __||  _  | | | | | |\/| ||  __| 	*
*	| |\ \| |___| | | | |/ /  | |  | || |___ 	*
*	\_| \_\____/\_| |_/___/   \_|  |_/\____/ 	*
*							*
*********************************************************

                       
 

TFTP Project - Iteration 3
SYSC 3303
Group 4

Adam Staples			[100978589]
Dan Hogan			[100929795]
Jason Van Kerkhoven		[100974276]
Nathaniel Charlebois		[100964496]
Sarah Garlough			[100965386]

30/09/2016


-----------------------------------------------------------
TEAM LOGISTICS

	Adam Staples
		- Bug Fixes
		- Timing Diagram
		
	Dan Hogan
		- Bug Fixes
		- Small Features
		- Readme
	
	Jason Van Kerkhoven
		- Refactored UI

	Nathaniel Charlebois
		- Server side re-transmission and timeout protocols 
		- Client side re-transmission and timeout protocols 
		- Bug Fixes
	
	Sarah Garlough
		- Host duplicate and lost functionality


-----------------------------------------------------------
CONTENTS:
	ServerThread.java	
	TFTPCLient.java		
	TFTPHost.java		
	TFTPReadThread.java	
	TFTPReader.java		
	TFTPServer.java		
	TFTPWriteThread.java	
	TFTPWriter.java		
	.project
	.classpath
	ReadMe.txt
	1ByteData.txt
	305ByteData.txt
	511ByteData.txt
	512ByteData.txt
	513ByteData.txt
	Oxford_Medical_Publications_Manual _of_Surgery.txt
	UIFramework.java
	RRQ_Error_Access_Violation.pdf
	RRW_Error_File_Not_Found.pdf
	RRQ_Typical_Case.pdf
	WRQ_Error_DiskFull.pdf
	WRQ_Error_NoPermission.pdf
	WRQ_Typical_Case.pdf

------------------------------------------------------------
SET UP INSTRUCTIONS:
	
	1.	Load all java files into Eclipse workspace/project
	2.	Build project
	
	RRQ Instructions
	1. 	Run Host
	2.	Run Server.java
	3. 	Choose file dump directory from popup filechooser
	4.  Enter true(Verbose) or false(Quite) into console
	5.	Run Client.java*
	6.  Enter help on the client to see instructions
	7.  Enter RRQ FileName Mode, with filename being the file you wish to read.
	8.	View Output in server and client
	9.  Use the x on the top of UI window to exit
	
	WRQ Instructions
	1. 	Run Host
	2.	Run Server.java
	3. 	Choose file dump directory from popup filechooser
	4.  Enter true(Verbose) or false(Quite) into console
	5.	Run Client.java*
	6.  Enter help on the client to see instructions
	7.  Enter WRQ Mode
	8.	A File Explorer will appear to select the directory you would like to write to on the server.
	9.	View Output in server and client
	10.  Use the x on the top of UI window to exit


	*Note that both programs must be running concurrently for 
	correct results*

------------------------------------------------------------
FILE INFORMATION:

ServerThread.java

	Abstract class designed to lay base criteria and behaviors for other
	threads in project.


TFTPCLient.java

	Makes a read or write request to either a TFTPServer or a
	TFTPHost. When read, sends a datagram:RRQ/WRQ, then proceeds
	to receive datagrams:DATA from the server/host. Sends an
	ACK to server after each datagram is received. Writes all
	incoming data to a file.
	Client can also make a write request to the server. It sends
	a packet to the server, and waits for an ACK to be sent in response.
	Loops until all data is sent. Packet specification and type can be
	found in TFTP REFERENCE section of READ ME.
	

TFTPHost.java

	Acts as an intermediate host between the server and clients. If in test 
	mode all messages will flow through the host were they can be manipulated.
	The intermediate host has the ability to lose, delay, duplicate or forward 
	packets normally.


TFTPReadThread.java

	Is initialized by the TFTPServer to complete a read request TFTP file transfer with the client. 
	Initially sends data to the request packet port and then waits to receive acknowledgements. Completes 
	the read process by reading data less than the maximum size (512).  
	

TFTPReader.java

	Takes a file and divides it into 512 Byte sections. Links these sections
	together as a linked list. Used to read files and split into 512 Byte long
	byte arrays (as according to the DATA packet type specification for TFTP). 


TFTPServer.java

	Receives requests from client(s). Can perform both read or write requests.
	Write requests entail receiving the request from the client in the form
	of a HEADER packet, acknowlegding said packet (sending an ACK), and then
	alternating between receiving packets/writing said data to a file and
	sending ACKs to the client.
	Can also handle read requestions. Receives HEADER packet from client, procedes
	to read a file and send client DATA packets (512B max data size), waiting
	in between each packet for client to acknowledge (ACK).
	

TFTPWriteThread.java

	Is initialized by the TFTPServer to complete a Write request TFTP file transfer 
	with the client. Initially sends a block zero acknowledgement and waits to receive data. 
	Completes the write process by writing data less than the maximum size (512).  


TFTPWriter.java

	Passed byte array and file name by caller. Creates file name based off of passed 
	info, then writes byte array to file. If the file already exits, defaults to not 
	overwriting.
	
ConsoleUI.java

	Generates GUI. The GUI uses a JTextArea for the output and JTestField for the input. This class 
	is thread safe through internal synchronization.
	
UIFramework.java

	Defines the basic methods a GUI must provide if we choose to implement different GUI types in the future.
	
TestBench.java
	
	Launches an instance of the server, client and host for quick testing.

------------------------------------------------------------
TFTP REFERENCE
TFTP Formats

   Type   Op #     Format without header

          2 bytes    string   1 byte     string   1 byte
          -----------------------------------------------
   RRQ/  | 01/02 |  Filename  |   0  |    Mode    |   0  |
   WRQ    -----------------------------------------------
          2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------
          2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------
          2 bytes  2 bytes        string    1 byte
          ----------------------------------------
   ERROR | 05    |  ErrorCode |   ErrMsg   |   0  |
          ----------------------------------------




















