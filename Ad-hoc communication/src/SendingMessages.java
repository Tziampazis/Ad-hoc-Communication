import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * This is the thread that sends messages when the send button is pressed in the GUI
 * @author Christodoulos
 *
 */
public class SendingMessages extends Thread implements ActionListener, ItemListener{
	
	ForwardTable ft;
	MulticastSocket sock = null;
	Map<Integer,DatagramPacket> allpktsend = new HashMap<Integer, DatagramPacket>();
	MultiReceiving ackToRem = new MultiReceiving(sock,ft, MultiReceiving.idstatic); 
	Scanner peer = new Scanner(System.in);
	String msg;
	Random rand = new Random();	
	String Ipstatic = "192.168.5.";
	DatagramPacket frstpacket;
	public static int num;
	static String IpchoicePeer;
	int checkretransmition;
	int mPort = 12347;
	static String peerToString = "";
	String scaned = "";
	
	public SendingMessages(MulticastSocket sock, ForwardTable ft) throws IOException{
		this.sock = sock;
		this.ft = ft;
	}
	
	/**
	 * When a user chooses to whom to send from the GUI,
	 * this method sets the destination
	 * @param choice
	 */
	public void choosePeer(int choice) {
		

//THIS IS FOR THE TUI		
		
		
//		peer = new Scanner(System.in);
//
//		String enter = peer.next();
//		
//		try {
//			choice = Integer.parseInt(enter);
//
//		} catch (NumberFormatException e){
//			System.out.println("Invalid input");
//		}
//		try {
//			while(!ft.contains(InetAddress.getByName("192.168.5."+choice)) || choice == MultiReceiving.idstatic
//					|| ft.getCost(InetAddress.getByName("192.168.5."+choice)) >= MultiReceiving.MAXHOPS){
//				System.out.println("You cannot reach this computer");
//				enter = peer.next();
//				
//				try {
//					choice = Integer.parseInt(enter);
//
//				} catch (NumberFormatException e){
//					System.out.println("Invalid input");
//						
//				}
//			}
//		} catch (UnknownHostException e1) {
//						e1.printStackTrace();
//		}
//				
				
		//this is the destination
		peerToString = Integer.toString(choice);
		IpchoicePeer = Ipstatic + peerToString;
		setIP(IpchoicePeer);
				
		setAckDes(peerToString);
		
		System.out.println("You chose " + IpchoicePeer);
				
				
	}
	

	public void run(){
		//choose peer from the method

		while (true) {
		
			/*
			 * This is the ACK number. We need to make sure that it is not the same as the previous one
			 */
			int num1 = rand.nextInt(5000);
			while (num1 == this.num -1 ) {
				num1 = rand.nextInt(5000);

			}
			
			//THIS IS FOR THE TUI
			
//			//String nonce = num.;
//			Scanner scan = new Scanner(System.in);
//			System.out.println("Give a message and hit ENTER : use '_change' to change peer!");
//			System.out.println("-----------------------------");
//			String scaned = scan.nextLine();
//			while (scaned.isEmpty()){
//				System.out.println("Please fill in with a message : ");
//				scaned = scan.nextLine();
//			}
//			
//			
			if (!peerToString.equals ("")) {//if the user has actually chosen a destination
				try {
					/*
					 * If the destination is in our forward table and not unreachable
					 * and the user has typed a message and pressed "send"
					 */
					if (!scaned.equals("") && ft.contains(InetAddress.getByName("192.168.5."+peerToString)) && 
							Integer.parseInt(peerToString) != MultiReceiving.idstatic &&
							ft.getCost(InetAddress.getByName("192.168.5."+peerToString)) < MultiReceiving.MAXHOPS){
						//Constructs the message Sending:Destination:nonce:message;
						msg = 	ackToRem.getMYID() +";"+ peerToString +";"+num1 + ";" + scaned ;
					
						setNum(num1);
						 
						InetAddress destination = null;
						InetAddress nextHop = null;
						try {
							destination = InetAddress.getByName(IpchoicePeer);
							nextHop = ft.getNextHop(destination);
							
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						 
						 DatagramPacket frstpacket = new DatagramPacket(Encryption.encrypt(msg), Encryption.encrypt(msg).length,
						                             nextHop, sock.getLocalPort());
						
						 try {
								sock.send(frstpacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
						 
						 //put every packet send with its nonce+1
						 allpktsend.put(num+1, frstpacket);
						
						 //timewait to get ack
						 try {
								TimeUnit.MILLISECONDS.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						//assing received ack 
						 checkretransmition = ackToRem.getAckId();
						 
						 				//Do while retransmit packet is empty.
										 while(!allpktsend.isEmpty()){						 
										 
										 if (allpktsend.containsKey(checkretransmition)){
											 allpktsend.remove(checkretransmition);
										 }
										 
										 try {
												if (ft.getCost(InetAddress.getByName(IpchoicePeer)) >= 5) {
													System.out.println("This user is unreachable");
													break;
												}
											} catch (UnknownHostException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
											
										 //retransmit
											 for(Integer pkt : allpktsend.keySet()){
												 try {
														sock.send(allpktsend.get(pkt));

													} catch (IOException e) {
														e.printStackTrace();
													}
												
													break; 
												 
											 }
											//timewait to get ack
											 try {
													TimeUnit.MILLISECONDS.sleep(500);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
							
											//assigning received ack 
											 checkretransmition = ackToRem.getAckId();
						 }
						//we set scaned to be empty again because it should be filled
						//from the GUI
						scaned = "";				 				 
					}
				} catch (NumberFormatException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
		}
		
	}
	
	public void setNum (int num){
		this.num = num;
	}
	
	public int getNum(){
		return num;
	}
	
	public void setIP(String IpchoicePeer){
		this.IpchoicePeer = IpchoicePeer;
		
	}
	public String getIP(){
		return IpchoicePeer;
	}
	
	public void setAckDes(String peerToString){
		this.peerToString = peerToString;
	}
	public String getAckDes(){
		return peerToString;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		 String command = e.getActionCommand(); 
	        System.out.println(command);
	        if(command.equals("send")) {
	        	this.scaned = Run.sendArea.getText();
	        }
	}

	/**
	 * This is for the JComboBox of the GUI to work
	 * by setting the destination
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {

        int item = 0;

        if(e.getStateChange() == ItemEvent.SELECTED && !e.getItem().equals("")) {
        	item = Integer.parseInt((String) e.getItem());
        	choosePeer(item);
        }
        
	}
}






	


