/**
*Class:             Console.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    12/010/2016                                              
*Version:           1.0.0                                                      
*                                                                                   
*Purpose:           Generic console for basic output/inputs
* 
* 
*Update Log:		v1.0.0
*						- null
*/
package ui;

//important libraries
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class Console extends JPanel implements UIFramework, ActionListener
{
	//declaring local instance variables
	private boolean verbose;
	private String ID;
	private JTextField outputField;
	private JTextArea inputLine;
	
	
	//generic constructor
	public Console(String name)
	{
		//set up layout, save ID
		super(new GridBagLayout());
		ID = name;
		
		//create text fields for output and input
		outputField = new JTextField(45);
		outputField.addActionListener(this);
		inputLine = new JTextArea(35,45);
		inputLine.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(inputLine);

        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
        add(outputField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.weighty = 5.0;
        add(scrollPane, c);
	}
	
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(new Console("ID"));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	
	@Override
	public void print(String printable) 
	{		
	} 

	
	@Override
	public void input(String in) 
	{
	}

	
	@Override
	public void inputAndPrint(String in) 
	{
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{	
        String text = outputField.getText();
        inputLine.append(text + "\n");
        outputField.selectAll();

        //Make sure the new text is visible, even if there
        //was a selection in the text area.
        inputLine.setCaretPosition(inputLine.getDocument().getLength());
	}
	

	//for testing
	public static void main (String[] args) 
	{
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		
	}

}
