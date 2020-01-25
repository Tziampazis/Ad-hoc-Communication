import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * This is created to be called in MultiReceiving in order to create an ACK message
 * and then forward it or send it to the sender
 * @author Christodoulos
 *
 */
public class SendingAck {
	
	String msg;
	MulticastSocket sock;
	InetAddress destination;
	String destString;
	InetAddress nextHop ;
	SendingMessages ip ;
	ForwardTable ft;
	MultiReceiving ackToSend;
	
	public SendingAck(String msg, MulticastSocket sock, ForwardTable ft, String destination) {
		
		this.msg = msg;
		this.sock = sock;
		this.ft = ft;
		try {
			this.destination = InetAddress.getByName(destination);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.destString = destination;

		
	}
	
	public void send() {
		
		nextHop = ft.getNextHop(destination);
				
		String[] splited = destString.split("\\.");

		 //Construct a new packet with sender : Destination : ACK
		String finalack =MultiReceiving.idstatic +";"+ splited[3] +";"+msg ;
		if (nextHop != null) {
			DatagramPacket frstpacket = new DatagramPacket(Encryption.encrypt(finalack), Encryption.encrypt(finalack).length,
                    nextHop, sock.getLocalPort());

			//Send new packet with sender : destination : destination : ACK
			try {
				sock.send(frstpacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	

	}
}
