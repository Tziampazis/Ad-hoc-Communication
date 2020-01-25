import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.MulticastSocket;
import javax.swing.JComboBox;
import javax.swing.JFrame;
/**
 * This is the class that the user should run to start the application.
 * When the constructor is called, first the GUI is created and then the three
 * threads (Broadcasting, Multireceiving and SendingMessages) are created and started 
 * @author Yannis
 *
 */

public class Run extends JFrame
			implements WindowListener, MouseListener, KeyListener{
	

	public static final int MYID = 2; 	//This is the last digit of the  user's address. 
										//It should be set before using the application

	public static TextArea messageArea = null;
	public static TextField sendArea = null;
	public static String message = "";
	public static JComboBox<String> receivers = new JComboBox<String>();
	
	public static ForwardTable ft = new ForwardTable();
	
	MulticastSocket sock = null;
	
	static Thread receive;
	static Thread broadcast;
	static Thread sending;
	

	public Run() {

		try {
			sock = new MulticastSocket(12345);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		receive = new MultiReceiving(sock, ft, MYID);

		broadcast = new Broadcasting(sock, ft);

		try {
			sending = new SendingMessages(sock, ft);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		receive.start();
		
		broadcast.start();
		
		sending.start();
		
		this.addWindowListener(this);
		this.setSize(800, 600);
		this.setResizable(true);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		messageArea = new TextArea();
		messageArea.setEditable(false);
		this.add(messageArea,"Center");
		messageArea.setFont(new Font("Arial", Font.PLAIN, 16));
		
		Panel p = new Panel();
		p.setLayout(new FlowLayout());
		
		sendArea = new TextField(30);
		sendArea.addKeyListener(this);
		sendArea.setFont(new Font("Arial", Font.PLAIN, 16));
		message = sendArea.toString();
		
		p.add(sendArea);
		p.setBackground(new Color(221,221,221));
		
		Button send = new Button("send");
		send.addMouseListener(this);
		p.add(send);
		send.setActionCommand("send");
		send.addActionListener(new ButtonClickListener());
		send.addActionListener((ActionListener) sending);
		
		Button clear = new Button("clear");
		clear.addMouseListener(this);
		p.add(clear);
		clear.setActionCommand("clear");
		clear.addActionListener(new ButtonClickListener());
				
		receivers.addItem("");
		receivers.setSelectedItem("");
		receivers.setEditable(true);
		p.add(receivers);
		receivers.addItemListener((ItemListener) sending);
		
		this.add(p, "South");
		this.setVisible(true);
		sendArea.requestFocus();
	}
	
	public static void main(String[] args) throws IOException {
		Run g = new Run();
	
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * This class is used to make GUI react when the user presses a button
	 * @author Yannis
	 *
	 */
	class ButtonClickListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand(); 
	        
	        if(command.equals("send")) {
	        	if (receivers.getSelectedItem().equals("")) {
	        		messageArea.append("Please choose the destination \n");
	        	} else {
		        	String msg =  sendArea.getText();
		        	messageArea.append("To " + SendingMessages.IpchoicePeer + ": " + msg+"\n");
	        	}
	        } 
	        
	        if (command.equals("clear")) {
	        	messageArea.setText("");
	        }
			
		}
		
	}


}

