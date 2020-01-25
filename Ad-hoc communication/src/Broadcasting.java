import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * periodically broadcasts the forward table
 * @author Yannis
 *
 */
public class Broadcasting extends Thread {
	
	ForwardTable ft;
    MulticastSocket sock;
    
	public Broadcasting (MulticastSocket sock, ForwardTable ft) {
		this.sock = sock;
		this.ft = ft;
		
		//I add myself at the forwarding table
		try {
			ft.add(InetAddress.getByName("192.168.5."+MultiReceiving.idstatic), 
					InetAddress.getByName("192.168.5."+MultiReceiving.idstatic), 0);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

    public void run() {
                         
         InetAddress group;                  
                  
         while (true) {
        	 
        	try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  	
        	
        	byte[] bytes = ft.encode();
        	
			try {
				
				group = InetAddress.getByName("228.0.0.0");
								
				DatagramPacket frstpacket = new DatagramPacket(bytes, bytes.length,
						group, sock.getLocalPort());
				
				sock.send(frstpacket);

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
         }
         

        
//         sock.leaveGroup(group);
//         sock.close();
    }

}