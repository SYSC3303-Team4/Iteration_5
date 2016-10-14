/**
*Class:             Console.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    13/010/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Generic console for basic output/inputs
* 
* 
*Update Log:		v1.0.1
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
	
	
	//return input, set flag
	public synchronized String getInput()
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
				System.out.println("Error in putting thread to sleep");
				//error handling
			}
		}
		
		//return and set flags
		String ret = input;
		input = null;
		inputReady = false;
		return ret;
	}
	
	
	@Override
	public synchronized void print(String printable) 
	{
        outputArea.append(" ".concat(printable + "\n"));
        
        //magic code to make sure stuff appears
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	} 
	
	
	@Override
	public void printIndent(String printable)
	{
		print("        ".concat(printable));
	}
	
	
	@Override
	public synchronized void clear()
	{
		outputArea.setText(null);
	}
	
	public synchronized void println()
	{
		outputArea.append(" ".concat("\n"));
        
        //magic code to make sure stuff appears
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}


	//get input from inputLine then prep to clear
	private String inputAndPrint() 
	{
		//get input
		inputLine.selectAll();
		String inputStr =  inputLine.getText();
		
		//print input in proper format
		print(" >".concat(inputStr));
		
		//return input
		return inputStr;
		
	}


	@Override
	//enter key pressed
	public synchronized void actionPerformed(ActionEvent e) 
	{	
		//get input, set inputReady to 1, notify anybody waiting on input
		input = inputAndPrint();
		inputReady = true;
		notifyAll();
	}
	
	
	
	/*
	//for testing
	public static void main (String[] args) 
	{	
		ConsoleUI console = new ConsoleUI("Test Console UI");
		console.run();
		String input;
		
		console.print("Running Output Test...");
		console.printIndent("Raviolo");
		console.printIndent("Raviolo");
		console.printIndent("Give me the formioli");
		console.print("Test Complete");
		console.println();
		
		console.print("Running Input Test...");
		input = console.getInput();
		console.print("Input 1:    " + input);
		input = console.getInput();
		console.print("Input 2:    " + input);
		console.print("Test Complete");
		console.println();
		
		console.print("Running clear test...");
		console.print("Enter 'clear'");
		input = console.getInput();
		if (input.equals("clear"))
		{
			console.clear();
		}
		console.print("Test Complete");
	}
	*/
}
