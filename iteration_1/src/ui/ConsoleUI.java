/**
*Class:             Console.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    03/12/2016                                              
*Version:           2.2.1                               
*                                                                                   
*Purpose:           Generic console for basic plain text output/inputs.
*					Guaranteed thread-safe. Lots of neat methods that should make everyone's
*					life easier. Tailor-made for Carleton University SYSC3303 TFTP Project.
* 
* 
*Update Log:		v2.2.0
*						- constructor for externally handled keypress based ISR added
*					v2.1.1
*						- added some NEW LIT colors & color demo
*						- prettier error popups
*						- error method refactor
*							\--> generalized error popup method that all popups are generated from
*							 \--> error popup thread safe with external synchronization
*							  \--> console error printing now handled internally
*					v2.1.0
*						- console can run in either dark or light mode
*						- really just a vanity update
*						- standard operand error method added
*						- standard error method altered to generate thread-safe popup
*					v2.0.0
*						- console clears inputLine after each input rather than select all text
*						- keypress based ISRs added
*						- Able to use keys to scroll through previous inputs
*						- last 25 inputs stored for future access
*					v1.1.1
*						- new constructor added for ISR based inputs
*					v1.1.0
*						- getInput(bool x) method added to support older implementation of method as
*						  getInput()
*						- getInput(bool x) patched to properly return null
*						- proper input parsing method added --> getParsedInput(bool x)
*						- consolidated actionEvent(..) and getInputText() into a single method
*						- printByteArray(..) method changed
*						- added new test method
*						- output field now has word wrap (ie no more pesky horizontal scroll)
*					v1.0.2
*						- method added to print byte array
*						- formatting for input/output fixed
*					v1.0.1
*						- runs on new thread
*						- external synchronization added
*						- new methods added
*						- test method added in main()
*						- now can return inputs
*						- output area is now above input field (as it should be)
*						- many variations on print method added
*						- notifyAll() no longer causes total thread meltdown
*					v1.0.0
*						- very basic implementation of framework v1.0.0
*/

/*
 * 	~~~~~~~~~~~~~~~~~~ HOW TO USE ~~~~~~~~~~~~~~~~~~
 * 		1. 	create new console 	      --> Console myConsole = new Console("CONSOLE_NAME");
 * 		2.	run console on new thread --> myConsole.run();
 * 
 * 		PRINT regular lines of text
 * 		myConsole.print(someText)
 * 
 * 		PRINT indented text (useful for displaying packet contents)
 * 		myConsole.printIndented(someText)
 * 
 * 		PRINT a new line
 * 		myConsole.println()
 * 
 * 		CLEAR the console
 * 		myConsole.clear()
 * 
 * 		get INPUT from console (note that the thread calling this will wait until input is
 * 		ready in console - ie user presses enter)
 * 		String input = myConsole.getInput() 
 * 
 * 		Lastly, please note all variations of the print method have formating included in them
 * 		Additionally, note that all variations of print are to print a single line of text
 * 		Sample code is included in main (also acts to test this class)
 */
package ui;

//important libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ConsoleUI extends JPanel implements UIFramework, ActionListener, KeyListener, Runnable
{
	//declaring local class constants
	private static final int BUFFER_SIZE = 25;
	
	//declaring local instance variables
	private String ID;
	private JTextField inputLine;
	private JTextArea outputArea;
	private String input;
	private CappedBuffer inputBuffer;
	private boolean inputReady;
	
	
	//generic constructor (used for client)
	//TODO ALL CONSTUCTORS SHOULD USER A SINGLE MASTER CONSTRUCTOR
	public ConsoleUI(String name)
	{
		//set up layout, save ID, initialize
		super(new GridBagLayout());
		ID = name;
		inputReady = false;
		input = null;
		inputBuffer = new CappedBuffer(BUFFER_SIZE);
		
		//create text fields inputs
		inputLine = new JTextField(45);
		inputLine.addActionListener(this);
		inputLine.addKeyListener(this);
		
		//set text fields for output
		outputArea = new JTextArea(35,45);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);			//horizontal word wrap true
		outputArea.setWrapStyleWord(true);		//horizontal word wrap true
		JScrollPane scrollPane = new JScrollPane(outputArea);

        //Add text areas in gridlayout
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(scrollPane, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.weighty = 5.0;
        add(inputLine, c);
	}
	
	
	//constructor for ISR based input (used for server)
	//TODO ALL CONSTUCTORS SHOULD USER A SINGLE MASTER CONSTRUCTOR
	public ConsoleUI(String name, ActionListener listener)
	{
		//set up layout, save ID, initialize
		super(new GridBagLayout());
		ID = name;
		inputReady = false;
		input = null;
		inputBuffer = new CappedBuffer(25);
		
		//create text fields inputs
		inputLine = new JTextField(45);
		inputLine.addActionListener(listener);
		inputLine.addKeyListener(this);
		
		//set text fields for output
		outputArea = new JTextArea(35,45);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);			//horizontal word wrap true
		outputArea.setWrapStyleWord(true);		//horizontal word wrap true
		JScrollPane scrollPane = new JScrollPane(outputArea);

        //Add text areas in gridlayout
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(scrollPane, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.weighty = 5.0;
        add(inputLine, c);
	}
	
	
	//constructor for externally handled key-press triggered ISRs added (used for host)
	//TODO ALL CONSTUCTORS SHOULD USER A SINGLE MASTER CONSTRUCTOR
	public ConsoleUI(String name, KeyListener listener)
	{
		//set up layout, save ID, initialize
		super(new GridBagLayout());
		ID = name;
		inputReady = false;
		input = null;
		inputBuffer = new CappedBuffer(25);
		
		//create text fields inputs
		inputLine = new JTextField(45);
		inputLine.addActionListener(this);
		inputLine.addKeyListener(listener);
		inputLine.addKeyListener(this);
		
		//set text fields for output
		outputArea = new JTextArea(35,45);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);			//horizontal word wrap true
		outputArea.setWrapStyleWord(true);		//horizontal word wrap true
		JScrollPane scrollPane = new JScrollPane(outputArea);

        //Add text areas in gridlayout
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(scrollPane, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.weighty = 5.0;
        add(inputLine, c);
	}
	
	//swap color schemes
	//return true if valid color scheme
	public boolean colorScheme(String scheme)
	{
		Color background = null;
		Color text = null;
		scheme = scheme.toLowerCase();
		
		if (scheme.equals("demo") || scheme.equals("all"))
		{
			try
			{
				this.colorScheme("dark");
				this.print("'color dark'");
			    Thread.sleep(750);
			    
			    this.colorScheme("ocean");
			    this.print("'color ocean'");
			    Thread.sleep(750);
			    
			    this.colorScheme("matrix");
			    this.print("'color matrix'");
			    Thread.sleep(750);
			    
			    this.colorScheme("light");
			    this.print("'color light'");
			    Thread.sleep(750);
			    
			    this.colorScheme("halloween");
			    this.print("'color halloween'");
			    Thread.sleep(750);
			    
			    this.colorScheme("prettyinpink");
			    this.print("'color prettyinpink'");
			    Thread.sleep(750);
			    
			    this.colorScheme("xmas");
			    this.print("'color xmas'");
			    Thread.sleep(750);
			    
			    this.colorScheme("bumblebee");
			    this.print("'color bumblebee'");
			    Thread.sleep(750);
			    
			    this.colorScheme("bluescreen");
			    this.print("'color bluesceen'");
			    Thread.sleep(750);
			    
			    this.colorScheme("50shades");
			    this.print("'color 50shades'");
			    Thread.sleep(750);
			    
			    this.colorScheme("dark");
			}
			catch(InterruptedException ie)
			{
				this.printError("Interrupted Exception", "Error putting thread to sleep");
			}
		}
		else if(scheme.equals("light"))
		{
			background = Color.WHITE;
			text = Color.BLACK;
		}
		else if (scheme.equals("dark"))
		{
			background = Color.BLACK;
			text = Color.WHITE;
		}
		else if (scheme.equals("ocean"))
		{
			background = Color.CYAN;
			text = Color.DARK_GRAY;
		}
		else if (scheme.equals("matrix"))
		{
			background = Color.BLACK;
			text = Color.GREEN;
		}
		else if (scheme.equals("prettyinpink"))
		{
			background = Color.BLACK;
			text = Color.MAGENTA;
		}
		else if (scheme.equals("halloween"))
		{
			background = Color.BLACK;
			text = Color.ORANGE;
		}
		else if (scheme.equals("xmas") || scheme.toLowerCase().equals("christmas"))
		{
			background = Color.RED;
			text = Color.GREEN;
		}
		else if (scheme.equals("bumblebee"))
		{
			background = Color.BLACK;
			text = Color.YELLOW;
		}
		else if (scheme.equals("feelingblue") || scheme.equals("bluescreen"))
		{
			background = Color.BLUE;
			text = Color.WHITE;
		}
		else if (scheme.equals("50shades"))
		{
			background = Color.DARK_GRAY;
			text = Color.LIGHT_GRAY;
		}
		else
		{
			return false;
		}
		
		//set outputArea colors
		outputArea.setBackground(background);
		outputArea.setForeground(text);
		outputArea.setCaretColor(text);
		//set inputLine colors
		inputLine.setBackground(background);
		inputLine.setForeground(text);
		inputLine.setCaretColor(text);
		return true;
	}
	
	@Override
	//final setup for console, set up window for visibility
	public void run() 
	{
        //set up window
        JFrame frame = new JFrame(ID);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add contents to the window.
        frame.add(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	
	//return PARSED input, set flag
	public String[] getParsedInput(boolean wait)
	{
		//call method to get input
		String in = getInput(wait);
		
		//split and return
		if (in != null)
		{
			return in.split(" ");
		}
		else
		{
			return null;
		}
	}
	
	
	//added for backwards compatibility with other version of method
	public String getInput()
	{
		return getInput(true);
	}
	
	
	//return RAW input, set flag
	public synchronized String getInput(boolean wait)
	{
		//wait for valid input
		if(wait)
		{
			//wait until input is detected
			while(!inputReady)
			{
				try
				{
					wait();
				}
				catch(Exception e)
				{
					System.out.println("!Error in putting thread to sleep!");
					//error handling
				}
			}
			
			//return and set flags
			String ret = input;
			input = null;
			inputReady = false;
			return ret;
		}
		//return whatever input is
		else
		{
			if(inputReady)
			{
				//return and set flags
				String ret = input;
				input = null;
				inputReady = false;
				return ret;
			}
			else
			{
				return null;
			}
		}
	}
	
	
	@Override
	//master print method for console, appends text to a new line in outputArea
	public synchronized void print(String printable) 
	{
        outputArea.append("    ".concat(printable + "\n"));
        
        //magic code to make sure stuff appears
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}
	
	
	@Override
	//generate a popup for a generic error
	public void printError(String errorType, String errorMsg)
	{
		printPopUp(
					errorMsg,
					errorType + " Error",
					JOptionPane.ERROR_MESSAGE,
					("ERROR: " + errorMsg)
					);
	}
	
	
	//print a syntax error to the outputArea
	public void printSyntaxError(String errorMsg)
	{
		print("SYNTAX ERROR - " + errorMsg);
	}
	
	
	//generate a popup for a TFTP error
	public void printTFTPError(int errorCode, String errorMsg)
	{
		printPopUp(
					"TFTP Error Type: " + errorCode + "\n" + errorMsg, 
					"TFTP Error", 
					JOptionPane.ERROR_MESSAGE,
					"TFTP ERROR Type: " + errorCode + " - " + errorMsg
					);
		
	}
	
	
	//master method for printing a popup message, prints the same message to console outputArea as well
	private synchronized void printPopUp(String message, String title, int messageType, String consolePrint)
	{
		if (consolePrint != null)
		{
			print(consolePrint);
		}
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}
	
	
	//prints a messages that file transfer is complete, generates a popup
	public void printCompletion(String transferType)
	{
		printPopUp("File Transfer Complete!", "", JOptionPane.INFORMATION_MESSAGE, 
				"---------------------- " + transferType.toUpperCase() + "COMPLETE ----------------------");
	}
	
	
	@Override
	//print indented text to outputArea
	public void printIndent(String printable)
	{
		print("           ".concat(printable));
	}
	
	
	@Override
	//clear the outputArea
	public synchronized void clear()
	{
		outputArea.setText(null);
	}
	
	
	//print an empty line to the outputArea
	public synchronized void println()
	{
		outputArea.append("".concat("\n"));
        
        //set caret to the end of text
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}
	
	
	//make our life easier by having a single method to print a byte array to the screen in hex format
	public synchronized void printByteArray(byte[] b, int size)
	{
		String printable = "Cntn:    ";
		for(int i = 0; i < size; i++)
		{
			printable = printable + (String.format("%02x", b[i])).toUpperCase() + " ";
			//printable = (printable + Integer.toHexString(b[i] & 0xFF) + " ");
		}
		printIndent(printable);
	}


	@Override
	//enter key pressed
	public synchronized void actionPerformed(ActionEvent e) 
	{		
		//get input, save to input field and inputBuffer, clear input line
		input =  inputLine.getText();
		inputLine.setText("");
		inputBuffer.push(input);
		
		//print input in proper format
		outputArea.append(" >" + input + "\n");
		//magic code to make sure stuff appears
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
		
		//set inputReady to true, notify anybody waiting on input
		inputReady = true;
		notifyAll();
	}
	
	
	@Override
	//up or down key pressed
	public synchronized void keyPressed(KeyEvent e) 
	{
		//declaring local method variables
	    int keyCode = e.getKeyCode();
	    String bufferReturn = null;
	    
	    //keycode handler
	    switch( keyCode ) 
	    { 
	    	//up arrow pressed (go back) (#goBackToRiverBuilding #never4get)
	        case (KeyEvent.VK_UP):
	        	bufferReturn = inputBuffer.getOlder();
	        	if (bufferReturn != null)
	        	{
	        		inputLine.setText(bufferReturn);
	        	}
	        	break;
	        
	        //down arrow pressed (go forward)
	        case (KeyEvent.VK_DOWN):
	        	bufferReturn = inputBuffer.getNewer();
	        	if (bufferReturn != null)
	        	{
	        		inputLine.setText(bufferReturn);
	        	}
	        	break;
	     }
	}
	
	
	@Override
	public void keyReleased(KeyEvent arg0) {}
	
	
	@Override
	public void keyTyped(KeyEvent arg0) {}
	
	
	//tester for console
	public void testAll()
	{
		//declaring local variables
		byte[] testArr1 = {(byte)0x00, (byte)0xFF, (byte)0x01, (byte)0x10, (byte)0x20, (byte)0x38};
		byte[] testArr2 = {(byte)0xFA, (byte)0xDE, (byte)0xD0, (byte)0x0A, (byte)0xDD};
		String input;
		String inputArr[];
		
		this.clear();
		
		//standard output+indent output test
		this.print("Running Output Test...");
		this.printIndent("Raviolo");
		this.printIndent("Raviolo");
		this.printIndent("Give me the formioli");
		this.print("Test Complete");
		this.println();
		
		//error message test
		this.printError("title","something went wrong");
		this.printTFTPError(5, "file not found");
		this.printTFTPError(99, "engineer.exe has stopped caring");
		this.printSyntaxError("NaN");
		this.printSyntaxError("generic error text");
		this.printSyntaxError("something else");
		
		//input with waiting test
		this.print("Running Input Test...");
		input = this.getInput();
		this.print("Input 1:    " + input);
		input = this.getInput();
		this.print("Input 2:    " + input);
		this.print("Test Complete");
		this.println();
		
		//parsed input
		this.print("Running Input Test w/ Parsing...");
		inputArr = this.getParsedInput(true);
		this.print("Input 1:");
		for(int i=0; i<inputArr.length; i++)
		{
			this.printIndent(inputArr[i]);
		}
		this.print("Running Input Test w/ Parsing...");
		inputArr = this.getParsedInput(true);
		this.print("Input 2:");
		for(int i=0; i<inputArr.length; i++)
		{
			this.printIndent(inputArr[i]);
		}
		this.print("Running Input Test w/ Parsing...");
		inputArr = this.getParsedInput(true);
		this.print("Input 3:");
		for(int i=0; i<inputArr.length; i++)
		{
			this.printIndent(inputArr[i]);
		}
		this.println();
		
		//input without wait test
		this.print("Running Input Test (no wait, expect null)...");
		input = this.getInput(false);
		this.print("Input 1:    " + input);
		this.print("Running Input Test (no wait, expect null)...");
		input = this.getInput(false);
		this.print("Input 2:    " + input);
		this.print("Running Input Test (no wait, expect value)...");
		this.print("PLEASE ENTER AN INPUT IN THE NEXT TEN(10) SECONDS");
		try 
		{
		    Thread.sleep(10000);
		} 
		catch(InterruptedException ex) 
		{
		    Thread.currentThread().interrupt();
		}
		input = this.getInput(false);
		this.print("Input 3:    " + input);
		this.print("Running Input Test (no wait, expect null)...");
		input = this.getInput(false);
		this.print("Input 4:    " + input);
		this.println();

		
		//test byte arr print
		this.print("Running byte arr test 1...");
		this.print("Expected:       00 FF 01 10 20 38");
		this.printByteArray(testArr1, testArr1.length);
		this.print("Running byte arr test 2...");
		this.print("Expected:       FA DE D0 0A DD");
		this.printByteArray(testArr2, testArr2.length);
		
		//test clear
		this.print("Running clear test...");
		this.print("Enter any input");
		input = this.getInput();
		this.clear();
		
		//test color set
		this.print("Running color scheme test...");
		this.colorScheme("demo");
		this.println();
		
		this.print("Test Complete");
	}
	
	
	
	//for testing
	/*
	public static void main (String[] args) 
	{	
		ConsoleUI console = new ConsoleUI("Test Console UI");
		console.run();
		console.testAll();
	}
	*/
}
