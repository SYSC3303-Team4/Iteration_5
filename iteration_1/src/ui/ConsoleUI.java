/**
*Class:             Console.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    07/11/2016                                              
*Version:           1.1.0                                                      
*                                                                                   
*Purpose:           Generic console for basic output/inputs
* 
* 
*Update Log:		v1.1.0
*						- getInput(bool x) method added to support older implementation of method as
*						  getInput()
*						- getInput(bool x) patched to properly return null
*						- proper input parsing method added --> getParsedInput(bool x)
*						- consolidated actionEvent(..) and getInputText() into a single method
*						- printByteArray(..) method changed
*						- added new test method
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



public class ConsoleUI extends JPanel implements UIFramework, ActionListener, Runnable
{
	//declaring local instance variables
	private String ID;
	private JTextField inputLine;
	private JTextArea outputArea;
	private String input;
	private boolean inputReady;
	
	
	//generic constructor
	public ConsoleUI(String name)
	{
		//set up layout, save ID, initialize
		super(new GridBagLayout());
		ID = name;
		inputReady = false;
		input = null;
		
		//create text fields for output and input
		inputLine = new JTextField(45);
		inputLine.addActionListener(this);
		outputArea = new JTextArea(35,45);
		outputArea.setEditable(false);
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
		return in.split(" ");
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
	public synchronized void print(String printable) 
	{
        outputArea.append("    ".concat(printable + "\n"));
        
        //magic code to make sure stuff appears
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	} 
	
	
	@Override
	public void printIndent(String printable)
	{
		print("           ".concat(printable));
	}
	
	
	@Override
	public synchronized void clear()
	{
		outputArea.setText(null);
	}
	
	
	public synchronized void println()
	{
		outputArea.append("".concat("\n"));
        
        //magic code to make sure stuff appears
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}
	
	
	public synchronized void printByteArray(byte[] b, int size)
	{
		String printable = "Cntn:    ";
		for(int i = 0; i < size; i++)
		{
			printable = printable + (String.format("%02x", b[i])).toUpperCase() + " ";
			//printable = (printable + Integer.toHexString(b[i] & 0xFF) + " ");
			System.out.println(printable);
		}
		printIndent(printable);
	}


	@Override
	//enter key pressed
	public synchronized void actionPerformed(ActionEvent e) 
	{		
		//get input, save to input field
		inputLine.selectAll();
		input =  inputLine.getText();
				
		//print input in proper format
		outputArea.append(" >" + input + "\n");
		//magic code to make sure stuff appears
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
		
		//set inputReady to true, notify anybody waiting on input
		inputReady = true;
		notifyAll();
	}
	
	
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
		this.print("Enter 'clear'");
		input = this.getInput();
		if (input.equals("clear"))
		{
			this.clear();
		}
		
		this.print("Test Complete");
	}
	
	
	/*
	//for testing
	public static void main (String[] args) 
	{	
		ConsoleUI console = new ConsoleUI("Test Console UI");
		console.run();
		console.testAll();
	}
	*/
}
