*********************************************************
*	______ _____  ___ ______  ___  ___ _____ 	    	*
*	| ___ \  ___|/ _ \|  _  \ |  \/  ||  ___|       	*
*	| |_/ / |__ / /_\ \ | | | | .  . || |__         	*
*	|    /|  __||  _  | | | | | |\/| ||  __|        	*
*	| |\ \| |___| | | | |/ /  | |  | || |___        	*
*	\_| \_\____/\_| |_/___/   \_|  |_/\____/        	*
*                                                       *
*********************************************************
		== best viewed in notepad++ ==                       
 

TFTP Project - Iteration 4
SYSC 3303
Group 4

Adam Staples            [100978589]
Dan Hogan               [100929795]
Jason Van Kerkhoven     [100974276]
Nathaniel Charlebois    [100964496]
Sarah Garlough          [100965386]

26/11/2016


-----------------------------------------------------------
TEAM LOGISTICS

	Adam Staples
		- Timing Diagram
		- Testing new iteration 4 features
		- Improvements to server output text
		
	Dan Hogan
		- Bug Fixes
		- Testing new iteration 4 features
		- Testing old iteration 3 features 
		- Code clean-up in server
	
	Jason Van Kerkhoven
		- UI updates for new errors
		- Created DataArtisan.java and TrashFactory.java class
		- Client error packet handling debugging + re-factoring
		- Add data error in host implementation
		- README

	Nathaniel Charlebois
		- Implemented unknown TID on server/client
		- Implemented unknown opcode on server/client
		- Implemented malformed payload detection on server/client
		- Bug fixes on client and server
		- Testing new iteration 4 features
	
	Sarah Garlough
		- Implemented opcode error in host
		- Implemented TID error in host
		- Implemented blocknum error in host
		- Implemented mode error in host


-----------------------------------------------------------
CONTENTS:
	
	SOURCE CODE
	==============================
	Package: scr
		ServerThread.java	
		TFTPCLient.java		
		TFTPHost.java		
		TFTPReadThread.java	
		TFTPReader.java		
		TFTPServer.java		
		TFTPWriteThread.java	
		TFTPWriter.java
		TestBench.java
		
	Package: ui
		UIFramework.java
		ConsoleUI.java
		
	Package: inputs
		Input.java
		InputStack.java
	
	Package: helpers
		DatagramArtisan.java
		TrashFactory.java

	

	TEST FILES
	==============================
	Test Files:
		1ByteData.txt
		305ByteData.txt
		511ByteData.txt
		512ByteData.txt
		513ByteData.txt
		Oxford_Medical_Publications_Manual_of_Surgery.txt
		Don'tstopBelievin.txt
	


	DIAGRAMS & FIGURES
	==============================
	Timing Diagrams:
		ALTERED_BLOCK_NUMBER.jpg
		ALTERED_PACKET_PORT.jpg
		INCORRECT_MODE_AT_REQUEST.jpg
		INCORRECT_OPCODE.jpg
		MALFORMED_PAYLOAD
	


	MISCELLANEOUS
	==============================
	Miscellaneous:
		.project
		.classpath
		README.txt




SET UP INSTRUCTIONS
------------------------------------------------------------
	
	1.	Load all java files into Eclipse workspace/project
	2.	Build project
	
	BASIC RRQ Instructions
	==============================
	1. 	Run testBench.java(Runs TFTPClient, TFTPHost, TFTPServer)	
	3. 	Choose the Server's file dump directory from popup filechooser
	4.	Select desired parameters in TFTPClient, TFTPHost, TFTPServer
	5.	Run Client.java
	6.  Enter help on the client to see instructions
	7.  Enter RRQ 'Filename', with 'Filename' being the file you wish to read
		(please note that it must be the full file name, ie, the extension must be included).
	8.	View Output in server and client
	


	BASIC WRQ Instructions
	==============================
	1. 	Run testBench.java(Runs TFTPClient, TFTPHost, TFTPServer)	
	3. 	Choose the Server's file dump directory from popup filechooser
	4.	Select desired parameters in TFTPClient, TFTPHost, TFTPServer
	5.	Run Client.java
	6.  Enter help on the client to see instructions
	7.  Enter WRQ
	8.	A file explorer will pop up. Select the file you wish to write to the server from there
	9.	View Output in server and client


	
	
OPERATING INSTRUCTIONS
------------------------------------------------------------

	USING THE UI
	==============================
	Instances of TFTPClient, TFTPHost, and TFTPServer class are designed to work with the standard UI 
	produced for this project, ConsoleUI.
	
	By default, verbose mode is set to false for all of the classes listed above. Additionally, test
	mode by default is set to false for any instance of TFTPClient.
	
	The ConsoleUI class functions using a operator-extended-operand format, that is, inputs are typed into
	the console using an "operator" to denote what you want it to do, followed by a number of "operands" 
	denoting how you want to do it. Operands can be thought of as parameters to a function, while the operator is 
	the function call itself. To illustrate, a standard function written verbose(Boolean b) would be called as 
	'verbose b'. Verbose acts as the operator, while the boolean value b acts as the operand. operators/operands 
	are separated by a single space.
	
	All acceptable inputs can be called for viewing in any of the three runnable classes 
	associated with this project (ie TFTPClient, TFTPHost, and TFTPServer) by typing 'help' into the 
	relevant console.
	
	ConsoleUI echos any user input to the screen in addition to any output generated by the associated 
	program. User input is denoted with a '>' character preceding it. Program output is show without a preceding '>'
	character.
	
	
	RUNNING THE TFTP SYSTEM
	==============================
	Instances of all required classes (TFTPClient, TFTPHost, TFTPServer) can be launched at the same time 
	through running TestBench.java. This class trivially creates 3 threads and run an instance of either client, 
	host, and server on each respectively. Alternatively, each class can be started and run separately instead 
	of using the provided test bench. It should be noted, however, that in the event either server, host, or 
	client encounters a critical and unexpected error where the thread would be terminated without warning, all three 
	classes will stop running. The solution to this is running all three classes OUTSIDE of the TestBench class, in 
	other words, run all three classes normally WITHOUT the use of TestBench.java.
	
	Similarly, if all three classes are launched from the TestBench.java class, closing one by user command will result
	in the same outcome as previously stated. All instances will stop running. Therefore, during any testing where you will
	be planning on terminating either TFTPHost, TFTPClient, or TFTPServer, it is highly recommended that you run them outside
	of the TestBench.java class.
	
	Directly after the launch of TFTPServer, a dialog box will appear prompting you to select a directory for use.
	This is the directory which the server will read all files from, and write all files to. This directory 
	can be located anywhere on your machine. A directory MUST be chosen for the server to function. This directory can be
	changed at any time using the 'cd' command. This command will be explained in further detail further in the read me.
	
	It should also be noted for additional simplicity, the commands 'pull' and 'push' are used in addition to 
	specifying RRQ or WRQ. The use of push, pull, rrq, and wrq commands are given in greater detail below. The 
	'push' command opens a file explorer, which in turn allows for the selection of the file you wish to push 
	(write) to the server. The 'pull' command allows the client to read a file (RRQ) from the server, 
	and save it to the main directory (ie above src).
	
	
	
	SETTING UP ERRORS IN HOST
	==============================
	Errors are set using the input line in the host UI.  There are eight (8) errors that can be simulated in 
	the host. These errors can affect either a ACK-type (04) packet, a DATA-type (03) packet, a WRQ-type (02) packet, 
	or a RRQ-type (03) packet. Currently, the host cannot simulate errors on a ERR-type (05) packet.
	
	Errors are given a numerical code for representation. The numerical representation of each error type
	can be seen in the chart below:
	
		|-------------------|---------------|
		|	ERROR TYPE		|	ERROR CODE	|
		|-------------------|---------------|
		| Delay				|	0			|
		| Duplicate			|	1			|
		| Lose				|	2			|
		| Alter Mode		|	3			|
		| Add Garbage Data	|	4			|
		| Alter OpCode		|	5			|
		| Alter TID			|	6			|
		| Alter BlockNumber	|	7			|
		|-------------------|---------------|
	
	The command for delay of a packet is given as 'delay PT BN DL', where PT is the packet type, BN is
	the block number, and DL is the amount of SECONDS the delay the packet by. The packet type can either 
	be given as a string (ie 'data', 'ack', 'rrq', 'wrq') or as the integer representation for each packet type 
	(1 for RRQ, 2 for WRQ, 3 for DATA, 4 for ACK). BN represents the block number of the packet, which is entered as a 
	standard integer. DL represents the delay applied to the packet in seconds, which is entered as a standard integer.
	For delay of either a RRQ or WRQ, enter 0 for the block number. 
	Alternatively, the numerical representation of delay can be used, given as '0 PT BN DL'.
	
	The command for duplication of a packet is given as 'dup PT BN', where PT is the packet type, and BN is
	the block number. The packet type can either be given as a string (ie 'data', 'ack', 'rrq', 'wrq') or as
	the integer representation for each packet type (1 for RRQ, 2 for WRQ, 3 for DATA, 4 for ACK).
	BN represents the block number of the packet, which is entered as a standard integer. For duplication of either
	a RRQ or WRQ, enter 0 for the block number.
	Alternatively, the numerical representation of duplication can be used, given as '1 PT BN'.
	
	The command for the lose of a packet is given as 'lose PT BN', where PT is the packet type, and BN is
	the block number. The packet type can either be given as a string (ie 'data', 'ack', 'rrq', 'wrq') or as
	the integer representation for each packet type (1 for RRQ, 2 for WRQ, 3 for DATA, 4 for ACK).
	BN represents the block number of the packet, which is entered as a standard integer. For the lose of either
	a RRQ or WRQ, enter 0 for the block number.
	Alternatively, the numerical representation of duplication can be used, given as '2 PT BN'.
	
	The command for altering the mode of a RRQ or WRQ is given as 'mode PT STR', where PT is the packet type, and STR is
	the new mode. The packet type can either be given as a string (ie 'rrq', 'wrq') or as the integer representation for 
	each packet type (1 for RRQ, 2 for WRQ). STR represents the new mode and is entered a standard string WITHOUT any spaces.
	Alternatively, the numerical representation of mode can be used, given as '3 PT STR'.
	
	The command for adding extra data onto a DATA packet is given as 'delay PT BN NUM', where PT is the packet type, BN is
	the block number, and NUM is the amount of bytes to add. The packet type can either be given as a string (ie 'data'), 
	or as the integer representation for each packet type (3 for DATA). BN represents the block number of the packet, 
	which is entered as a standard integer. NUM represents the amount of bytes appended to the packet in seconds, 
	which is entered as a standard integer.
	Alternatively, the numerical representation of delay can be used, given as '4 PT BN NUM'.
	
	The command for changing the opcode of a packet is given as 'opcode PT BN OP', where PT is the packet type, BN is
	the block number, and OP is the new opcode. The packet type can either be given as a string (ie 'data', 'ack', 'rrq', 
	'wrq') or as the integer representation for each packet type  (1 for RRQ, 2 for WRQ, 3 for DATA, 4 for ACK). 
	BN represents the block number of the packet, which is entered as a standard integer. OP represents what to change the opcode
	to, and is entered as a standard 2bit integer. For opcode alterations of either a RRQ or WRQ, enter 0 for the block number. 
	Alternatively, the numerical representation of delay can be used, given as '5 PT BN OP'.
	
	The command for changing the TID of a packet is given as 'tid PT BN TID', where PT is the packet type, BN is
	the block number, and TID is the new TID address. The packet type can either be given as a string (ie 'data', 'ack', 'rrq', 'wrq') 
	or as the integer representation for each packet type (1 for RRQ, 2 for WRQ, 3 for DATA, 4 for ACK). BN represents the block 
	number of the packet, which is entered as a standard integer. TID represents the new TID of the packet, which is entered as a 
	standard integer. For changing the TID of either a RRQ or WRQ, enter 0 for the block number. 
	Alternatively, the numerical representation of delay can be used, given as '6 PT BN TID'.
	
	The command for changing the block number of a packet is given as 'blocknum PT BN NUM', where PT is the packet type, BN is
	the block number, and NUM is the new block number. The packet type can either be given as a string (ie 'data', 'ack') or as the 
	integer representation for each packet type (3 for DATA, 4 for ACK). BN represents the block number of the packet, which is entered 
	as a standard integer. NUM represents the new block number of the packet, which is entered as a standard integer.
	Alternatively, the numerical representation of delay can be used, given as '7 PT BN NUM'.
	
	Examples of setting up errors are given below:
	
		|-------------------------------------------|-----------------------|-------------------|
		|	WRITTEN DESCIPTION OF ERROR        		|   STRING BASED INPUT	|	INT BASED INPUT	|
		|-------------------------------------------|-----------------------|-------------------|
		|	delay DATA packet 3 for 5 sec       	|   delay data 3 5      |	0 3 3 5			|
		|	delay ACK packet 2 for 10 sec       	|   delay ack 2 10      |	0 4 2 10		|
		|											|						|					|
		|	duplicate ACK packet 1              	|	dup ack 1           |	1 4 1			|
		|	duplicate DATA packet 3					|	dup data 3			|	1 3 3			|
		|											|						|					|
		|	lose DATA packet 2                  	|   lose data 2         |	2 3 2			|
		|	lose ACK packet 5						|	lose ack 5			|	2 4 5			|
		|											|						|					|
		|	set mode on RRQ packet to ASCII			|	mode rrq ASCII		|	3 1 ASCII		|
		|	set mode on WRQ packet to sysc3303		|	mode wrq sysc3303	|	3 2 sysc3303	|
		|											|						|					|
		|	add 10 extra random bytes to DATA 3		|	add data 3 10		|	4 3 3 10		|
		|	add 50 extra random bytes to DATA 8		|	add data 8 50		|	4 3 8 50		|
		|											|						|					|
		|	change the opcode in DATA 3 to 01		|	opcode data 3 1		|	5 3 3 1			|
		|	change the opcode in ACK 1 to 10		|	opcode ack 1 10		|	5 4 1 10		|
		|											|						|					|
		|	change the TID address of DATA 1 to 101	|	tid data 1 101		|	6 3 1 101		|
		|	change the TID address of ACK 4 to 24	|	tid ack 4 24		|	6 4 4 24		|
		|											|						|					|
		|	change the blocknum of DATA 5 to 6		|	blocknum data 5 6	|	7 3 5 6			|
		|	change the blocknum of ACK 1 to 22		|	blocknum ack 1 22	|	7 4 1 22		|
		|	change the blocknum of DATA 8 to 2		|	blocknum data 8 2	|	7 3 8 2			|
		|-------------------------------------------|-----------------------|-------------------|
	
	To view all of the errors to be simulated in the host, use the command 'errors', which will print the list 
	of all errors programmed into the simulator.
	
	It should be noted that BEFORE the file transfer begins, YOU MUST indicate to the host that you have entered 
	all the errors you wish to simulate. This is done by typing the 'run' command. If this is not done, host 
	WILL NOT pass through any data to server or client.
	
	
	SENDING RRQ AND WRQ TO SERVER
	==============================
	Sending RRQ and WRQ can be done trivially via several given commands. The rrq command is synonymous with the 
	pull command, similarly, the wrq command is synonymous with the push command. Files are written and read from 
	the directory specified upon server execution or through use of the 'cd' server command. On the client side, 
	files are read from anywhere on the computer. However, the files are always saved to the directory directly 
	above src. This is planned to be patched in a future version.
	
	If not specified, the assumed mode for all file transfers is assumed to be in standard 8-bit NETASCII 
	format. You can change this default mode through use of the 'mode NEWMODE' command in the client, where NEWMODE is
	the new default mode, entered as a standard string, containing no spaces.
	
	The use of all pull and push commands are given both below, and by calling the 'help' method.
	
	It should also be noted that for a RRQ, you must specify the file you wish to pull from the server via the 
	filename, given in text along with the command. However, for a WRQ, the file you wish to push to server is 
	selected via a file chooser.

	
	TFTPClient INSTRUCTIONS
	==============================
		The list of all accepted commands** to the client are given as:
		**Please note that words in all caps (ie B, MODE) are to denote a verbs
		
		'help'
			Print all commands and how to use to the console
			
		'clear'
			Clear the console output of all text
			
		'close'
			Shutdown the TFTPClient currently running
			
		'verbose B'
			Switch the client into verbose mode B, where B is a boolean variable.
			For example, for verbose to be set true type 'verbose true'. For verbose
			false, type 'verbose false'.
			
		'testmode B'
			Switch the client to regular mode and test mode. When in test mode, client
			passes all communications through the host (error simulator). When NOT in
			test mode, client passes all communications directly to the server.
			To toggle test mode on, type 'testmode true'. To toggle test mode off (ie
			run in regular mode) type 'testmode false'.
		
		'mode NEWMODE'
			Set the default mode of the client to NEWMODE, where NEWMODE is entered as a string
			into the console. It should be noted that the new mode cannot contain a space(s).
			
		'test'
			Run a simple test of UI functionally. Note that while this test is running,
			the UI will lose all functionality until the test is fully complete.
			
		'push MODE'
			Push a file FROM the client TO the server. Ergo, send a write request to the
			server. This command is synonymous to 'wrq MODE'. MODE is any string input.
			
		'push'
			Push a file FROM the client TO the server. Ergo, send a write request to the
			server. This ALWAYS sends the write request in the default mode.
			
		'pull FILE MODE'
			Requests a file FROM the server TO the client. Ergo, send a read request for file
			FILE in mode MODE. Both FILE and MODE are any strings. It should be noted that
			FILE cannot have any spaces in it.
			
		'pull FILE'
			Identical to 'pull FILE MODE'. MODE is set to the default mode of the client.
			
		'rrq FILE MODE'
			Identical to 'pull FILE MODE'.
			
		'wrq MODE'
			Identical to 'push MODE'
	
	
	TFTPHost INSTRUCTIONS
	==============================
		The list of all accepted commands** to the host are given as:
		**Please note that words in all caps (ie B, PT, BN, DL) are to denote verbs
		
		'help'
			Print all commands and how to use to the console
			
		'clear'
			Clear the console output of all text
			
		'close'
			Shutdown the host, freeing up all ports
			
		'verbose B'
			Switch the host into verbose mode B, where B is a boolean variable.
			For example, for verbose to be set true type 'verbose true'. For verbose
			false, type 'verbose false'.
			
		'run'
			Set the host to run for one (1) complete file transfer with any and all errors
			simulated. If no errors were entered, host will just pass the packets through normally.
			
		'test'
			Run a simple test of UI functionally. Note that while this test is running,
			the UI will lose all functionality until the test is fully complete.
			
		'errors'
			Print the sorted stack of all inputed errors you want the host to simulate.
			
		'delay PT BN DL'
			Add a delay type error to packet type PT, block number BN, delaying for DL.
			For instance, to delay ACK packet #2 by 1000ms, type 'delay ack 2 1000'. 
			Alternatively, type '0 4 2 1000'. PT can be either the integer
			code for packet type (ie 4 for ACK, 3 for DATA, 2 for WRQ, 1 for RRQ), or the
			written code as a string (ack, data, wrq, rrq).
			
		'dup PT BN'
			Add a duplicate type error to packet type PT, block number BN
			For instance, to duplicate ACK packet #2 by 1000ms, type 'dup ack 2'. 
			Alternatively, type 'dup 4 2'. PT can be either the integer
			code for packet type (ie 4 for ACK, 3 for DATA, 2 for WRQ, 1 for RRQ), or the
			written code as a string (ack, data, wrq, rrq).
			
		'lose PT BN'
			Add a lose type error to packet type PT, block number BN.
			For instance, to lose ACK packet #2, type 'lose ack 2'. 
			Alternatively, type '2 4 2'. PT can be either the integer
			code for packet type (ie 4 for ACK, 3 for DATA, 2 for WRQ, 1 for RRQ), or the
			written code as a string (ack, data, wrq, rrq).	
			
		'mode PT STRING'
			Change the mode of either a RRQ or WRQ packet. PT can either be 'rrq' or 'wrq'.
			The new mode of the request is set to STRING. For example, changing the mode of
			a RRQ to "ascii" would be written as: mode rrq ascii.
			
		'add PT BN NUM'
			Add NUM bytes of data to packet type PT, block number BN. The packet type (PT) must be
			of type DATA (denoted as 'data' in the console). For example, to add 12 bytes of garbage data
			to DATA packet 3: add data 3 12.
			
		'opcode PT BN OP'
			Change the opcode of packet type PT, block number BN to OP. OP must be a valid 2 byte integer.
			If for a RRQ or WRQ, BN should be entered as 0. For example, to set the opcode of a RRQ to 12:
			opcode rrq 0 12.
			
		'tid PT BN TID'
			Change the tid of packet type PT, block number BN to TID. TID must be entered as a valid TID address.
			If for a RRQ or WRQ, BN should be entered as 0. For example, to set the TID of DATA 3 to 1234:
			tid data 3 1234.
		
		'blocknum PT BN NUM'
			Change the block number of either a data packet or ack packet to NUM, with PT being either 'data' or 'ack' and
			BN being the original block number. For example, the change the block number of ACK 5 to 10: blocknum ack 5 10.
			
		'0 PT BN DL'
			Functions exactly the same as 'delay PT BN DL'.
			
		'1 PT BN'
			Functions exactly the same as 'dup PT BN'.
			
		'2 PT BN'
			Functions exactly the same as 'lose PT BN'.
		
		'3 PT STRING'
			Functions exactly the same as 'mode PT STRING'.
		
		'4 PT BN NUM'
			Functions exactly the same as 'add PT BN NUM'.
			
		'5 PT BN OP'
			Functions exactly the same as 'opcode PT BN OP'
			
		'6 PT BN TID'
			Functions exactly the same as 'tid PT BN TID'.
			
		'7 PT BN NUM'
			Functions exactly the same as 'blocknum PT BN NUM'.
		
		
	TFTPServer INSTRUCTIONS
	==============================
		The list of all accepted commands** to the host are given as:
		**Please note that words in all caps (ie B, PT, BN, DL) are to denote verbs
		
		'help'
			Print all commands and how to use to the console
			
		'clear'
			Clear the console output of all text
			
		'close'
			Shutdown the server, freeing up all ports
			
		'verbose B'
			Switch the server into verbose mode B, where B is a boolean variable.
			For example, for verbose to be set true type 'verbose true'. For verbose
			false, type 'verbose false'.			  
			
		'test'
			Run a simple test of UI functionally. Note that while this test is running,
			the UI will lose all functionality until the test is fully complete.
			
		'cd' 
			Change the working directory for server
			
		'path'
			Print the working directory for the server
				
				
				
			
FILE INFORMATION:			
------------------------------------------------------------

	ServerThread.java
	==============================
		Abstract class designed to lay base criteria and behaviors for other
		threads in project.


	TFTPCLient.java
	==============================
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
	==============================
		Acts as an intermediate host between the server and clients. If in test 
		mode all messages will flow through the host were they can be manipulated.
		The intermediate host has the ability to lose, delay, duplicate. 


	TFTPReadThread.java
	==============================
		Is initialized by the TFTPServer to complete a read request TFTP file transfer with the client. 
		Initially sends data to the request packet port and then waits to receive acknowledgements. 
		Completes the read process by reading data less than the maximum size (512).  
		

	TFTPReader.java
	==============================
		Takes a file and divides it into 512 Byte sections. Links these sections
		together as a linked list. Used to read files and split into 512 Byte long
		byte arrays (as according to the DATA packet type specification for TFTP). 


	TFTPServer.java
	==============================
		Receives requests from client(s). Can perform both read or write requests.
		Write requests entail receiving the request from the client in the form
		of a HEADER packet, acknowlegding said packet (sending an ACK), and then
		alternating between receiving packets/writing said data to a file and
		sending ACKs to the client.
		Can also handle read requestions. Receives HEADER packet from client, procedes
		to read a file and send client DATA packets (512B max data size), waiting
		in between each packet for client to acknowledge (ACK).
		

	TFTPWriteThread.java
	==============================
		Is initialized by the TFTPServer to complete a Write request TFTP file transfer 
		with the client. Initially sends a block zero acknowledgement and waits to receive data. 
		Completes the write process by writing data less than the maximum size (512).  


	TFTPWriter.java
	==============================
		Passed byte array and file name by caller. Creates file name based off of passed 
		info, then writes byte array to file. If the file already exits, defaults to not 
		overwriting.
		
		
	ConsoleUI.java
	==============================
		Generates GUI. The GUI uses a JTextArea for the output and JTestField for the input. This class 
		is thread safe through internal synchronization.
		
		
	UIFramework.java
	==============================
		Defines the basic methods a GUI must provide if we choose to implement different GUI types in 
		the future.
		
		
	TestBench.java
	==============================
		Launches an instance of the server, client and host for quick testing.

		
	Input.java
	==============================
		Basic datatype to hold the maximum-of-four values used to properly simulate an error.
		
		
	InputStack.java
	==============================
		A sorted stack of all errors to simulate. Sorted in terms of ascending block number, and packet-type 
		in the case of a tie.
		
		
	DatagramArtisan.java
	==============================
		Produce datagrams, and/or extract specific information from them.
		
	
	TrashFactory.java
	==============================
		Produce a random array of bytes, of a size specified by the method calling it.
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
<end>