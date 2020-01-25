import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This thread is used to receive messages and deal with them accordingly:
 * update the forward table/show it to the GUI/forward it to the repsective nextHop
 * @author Christodoulos, Yannis
 *
 */
public class MultiReceiving extends Thread {
	
	
	//neighbour -> last time I heard of him
	Map <InetAddress, Long> neighbours = new ConcurrentHashMap<>();

    MulticastSocket sock;
	ForwardTable ft;
	public static final int MAXHOPS = 3;
	InetAddress group;
	String Ipstatic = "/192.168.5.";
	static int idstatic ;
	SendingMessages mess;
	static int ackIdent ;
	Map<String , Long> receivedMsges = new ConcurrentHashMap<>();

    
	public MultiReceiving (MulticastSocket sock, ForwardTable ft, int idstatic) {
		this.sock = sock;
		this.ft = ft;
		this.idstatic = idstatic;

	}
	
	public void setAckIdent(int ackIdent){
		this.ackIdent = ackIdent;
	}
	public int getAckId(){
		return ackIdent;
	}
	
	public int getMYID(){
		return this.idstatic;
	}
	
	/*
	 * Shows if something is contained in a List<String[]>
	 */
	public boolean contains(List<String[]> list, String[] element) {
		boolean contains = false;
    	for (String[] element1 : list) {
    		if (Arrays.equals(element1, element)) {
    			contains = true;
    			break;
    		}
    	}
		return contains;
	}

	/**
	 * When a broadcasted forward table packet is received,
	 * this method is called to update the forward table accordingly
	 * @param bytes
	 * @param receive
	 */
	public void dealWithBroadcast(byte[] bytes, DatagramPacket receive) {
		 
		 ForwardTable receivedTable = ForwardTable.decode(bytes);
		
		 //Time that I received it
		 long timeReceived = System.currentTimeMillis();
		 
		 //if I don't have this neighbor, I add him to the list
		 if (!neighbours.containsKey(receive.getAddress())) {
			 
			 neighbours.put(receive.getAddress(), timeReceived);
			 
			 System.out.println("You can now send to " + receive.getAddress());
			 
			 //System.out.println("I added " + receive.getAddress() + " in neighbours");
			 
			 ft.add(receive.getAddress(), receive.getAddress(), 1); //if he is a neighbor, the cost is 1
			 String[] split = receive.getAddress().toString().split("\\.");
			 Run.receivers.addItem(split[3]);
			 
			 //if he is a neighbor, I update the timeReceived
		 } else if (neighbours.containsKey(receive.getAddress())) {
			 
			 //System.out.println("I updated the timeReceived!!!");
			 neighbours.replace(receive.getAddress(), timeReceived);

		 }
		 //I loop over the receivedTable's entries
		 for (Entry<InetAddress, Route> entry : receivedTable.getMap().entrySet()) {
			
			//check if node is in my forwarding table
			 if (ft.contains(entry.getKey())) {
				 
				 //if the new cost is smaller, I replace it in the ft and I give as nextHop the neighbor
				 if (entry.getValue().cost + 1 < ft.getCost(entry.getKey())) {
					 ft.replace(entry.getKey(), receive.getAddress() ,entry.getValue().cost + 1);
					 
					 //if the new cost is bigger and I have learned this route through my neighbor
					 //I should update the table anyway
				 } else if (entry.getValue().cost + 1 >= ft.getCost(entry.getKey()) &&
						 ft.getNextHop(entry.getKey()).equals(receive.getAddress())) {
					 
					 //if the distance becomes infinity, set it to infinity
					 if (entry.getValue().cost + 1 >= MAXHOPS) {
						 ft.replace(entry.getKey(), receive.getAddress() ,MAXHOPS);
						 
						 String[] split = entry.getKey().toString().split("\\.");
						 Run.receivers.removeItem(split[3]);
						 
						 System.out.println("You can no longer send to " + entry.getKey());


					 } else {
						 
						 ft.replace(entry.getKey(), receive.getAddress() ,entry.getValue().cost + 1);
						 
					 }
					 
				 }
				 	
				 
				 //if I do not have the Node in my Forward table
			 } else if (!ft.contains(entry.getKey())) {
				 				 
				 //if the cost exceeds max cost, make it unreachable
				 if (entry.getValue().cost + 1 >= MAXHOPS) {
					 
					 //set the cost to MAXHOPS
					 ft.add(entry.getKey(), entry.getValue().nextHop, MAXHOPS);
				 
					 System.out.println("Node " + entry.getKey() + " is now unreachable");

				 } else {
					 
					 System.out.println("You can now send to " + entry.getKey());

					 
					 ft.add(entry.getKey(), receive.getAddress(), entry.getValue().cost + 1);
					 
					 String[] split = entry.getKey().toString().split("\\.");
					 Run.receivers.addItem(split[3]);
				 }
				 
			
			 }
		 
		 
	 	}
		 
		 
	}


    public void run() {                 
         
 		try {
			
			group = InetAddress.getByName("228.0.0.0");

			sock.joinGroup(group);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

         
         while (true) {
      
             // 65KB is the maximum size
             byte[] buffered = new byte[65536];
             DatagramPacket receive = new DatagramPacket(buffered, buffered.length);
             try {

				sock.receive(receive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             
           //Create IP address for me to be filter out.
			String idstatictoString = Integer.toString(idstatic);
			String Ipfilter = Ipstatic +idstatictoString;
			String receivedIP = receive.getAddress().toString();
				
             
             byte[] bytes = receive.getData();
             
             /*
              * We did this because the empty spaces in the buffer
              * were not in fact empty but zeros and we needed to get rid of them
              */
             String str = "";
             for(byte b : bytes) {
            	 if(b == 0) {
            		 break;
            	 }
            	 str += (char) b;
             }

    		 String[] splittedOnDouble = str.split(";;;");
    		 //we check that we do not take into account the packets that we sent
    		 if (!receivedIP.equals(Ipfilter)){
    			 
    			 	//those are forward table packets
    			   if (splittedOnDouble[0].equals("@@@@@")) {

    	            	 dealWithBroadcast(bytes, receive);
    						 					
    				} else {
    					  /*
    		              * We did this because the empty spaces in the buffer
    		              * were not in fact empty but zeros and we needed to get rid of them
    		              */
						List<Byte> byteList = new ArrayList<>();
					     for(byte b : bytes) {
			            	 if(b == 0) {
			            		 break;
			            	 }
			            	 byteList.add(b);
			             }
					     //we decrypt the message
					     byte[] toDecryption = new byte[byteList.size()];
					     
					     int k = 0;
					     for (byte el : byteList) {
					    	 toDecryption[k] = el;
					    	 k++;
					     }
					     
					     String string = Encryption.decrypt(toDecryption);
						
						String[] parts = string.split(";");
						
						//This is a full message (neither forward table nor an ACK)
							if (parts.length >= 4){

										//Structure receiving 
										//sender
										String part1 = parts[0];
										//receiver
										String part2 = parts[1];
										String receiver = "192.168.5."+part2;
										String sender = "192.168.5."+part1;
										InetAddress receiverIP = null;
										try {
											receiverIP = InetAddress.getByName(receiver);
										} catch (UnknownHostException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										//nonce
										String part3 = Integer.toString(Integer.parseInt(parts[2]) + 1);
										
										/*
										 * Because we used semicolons (;) to separate the parts of the packet
										 * (destination/source/ackNumer/text) we encountered problems when the text 
										 * contained semicolons. We did the following to deal with this
										 */
										String part4 = "";
										if (parts.length == 4) {
											part4 = parts[3];
										} else {
											for (int i = 3; i < parts.length; i++) {
												if (i == 3) {
													part4 = parts[i];
												} else {
													part4 = part4 + ";"+ parts[i];		
												}
											}
										}
										
										/*
										 * When the message is for us, we show it to the user and 
										 * send an acknowledgement
										 */
								if (Ipfilter.equals("/"+receiver)){
									
									if (!receivedMsges.containsKey(string)){
										System.out.println(sender +" nonce : ["+ part3 + "] says : " + part4);
										Run.messageArea.append("From " + sender + ": " + part4+"\n");
										
									}
									//SEND BACK THE ACKN
									SendingAck ack = new SendingAck(part3, sock, ft, sender);
									ack.send();
									System.out.println("----------------");
									
									System.out.println("Give a message and hit ENTER : use '_change' to change peer!");

									
									/*THIS IS WHERE REROUTING IS TAKING PLACE
									 * if it is not for us and we have the destination in our forward table,
									 * we reroute it to the respective nextHop
									 */
								}else if (ft.contains(receiverIP)){
									InetAddress nextHop = ft.getNextHop(receiverIP);
									receive.setAddress(nextHop);
									try {
										sock.send(receive);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								//This is so that we do not show to the users messages that were retransmitted because of 
								//Ack loss
								if  (!receivedMsges.containsKey(string)){
									receivedMsges.put(string, System.currentTimeMillis());
								}
								
								//IN CASE WE RECEIVE AN ACK
							}else if (parts.length == 3){
								//CHECK THE ACK A IS RECEIVED
					             	
					             	//Ack parts separate
					             	//sender
					             	String part1 = parts[0];
									//receiver
									String part2 = parts[1];
									String receiver = "192.168.5."+part2;
									InetAddress receiverIP = null;
									try {
										receiverIP = InetAddress.getByName(receiver);
									} catch (UnknownHostException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									//ack received
									String part3 = parts[2];
										
								int ackedToCheck = Integer.valueOf(part3);
								ackIdent = ackedToCheck;
								setAckIdent(ackIdent);
								
								//to forward the ack
								if (Ipfilter.equals(receiver)){
								
									SendingAck ack = new SendingAck(part3, sock, ft, receiver);
									ack.send();
									System.out.println("----------------");
									
									System.out.println("Give a message and hit ENTER;");

									
								}else if (ft.contains(receiverIP)){
									InetAddress nextHop = ft.getNextHop(receiverIP);
									receive.setAddress(nextHop);
									try {
										sock.send(receive);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
								
							}
						
    				}
    	             
    		 }
    		     		 
          
             /*
              * Check when was the last time that I heard of a neighbour
              */
             
             long currentTime = System.currentTimeMillis(); 
            	 
            	 for (Entry <InetAddress, Long> entry: neighbours.entrySet()) {
                	 
                     long dt = currentTime - entry.getValue();
                     
                     double dtSeconds = dt / 1000.0;
                     
                     //System.out.println("The time dif is " + dt);
                    
                     if (dtSeconds > 10) {
                    	 ft.replace(entry.getKey(), entry.getKey(), MAXHOPS);
                    	 neighbours.remove(entry.getKey());
                    	 
						 System.out.println("You can no longer send to " + entry.getKey());
						 
						 String[] split = entry.getKey().toString().split("\\.");

						 Run.receivers.removeItem(split[3]);

                     }
                	 
                 } 
            	 
            	 for (Entry<String, Long> entry : receivedMsges.entrySet()){
            		 long dt = currentTime - entry.getValue();
            		 
            		 double dtSeconds = dt /1000.0;
            		 
            		 if ( dtSeconds >30){
            			 receivedMsges.remove(entry.getKey());
            		 }
            		 
            	 }
            
         
         } 

    }

}