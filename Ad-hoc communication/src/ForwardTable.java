import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the data structure that saves the forward table
 * @author Yannis
 *
 */

public class ForwardTable {
	
	Map<InetAddress, Route> forTable = new HashMap<>();
	
	public ForwardTable() {
		
	}
	
	public ForwardTable(Map<InetAddress, Route> forTable) {
		this.forTable = forTable;
		
	}
	
	public void delete(InetAddress toDelete) {
		forTable.remove(toDelete);
	}
	
	
	public void add(InetAddress address, InetAddress nextHop, int cost) {
		
		Route r = new Route();
		r.cost = cost;
		r.nextHop = nextHop;
		forTable.put(address, r);
	}
	
	public void replace(InetAddress address, InetAddress nextHop, int cost) {
		
		Route r = new Route();
		r.cost = cost;
		r.nextHop = nextHop;
		
		forTable.replace(address, r);
	}
	
	public void replace(InetAddress address, Route r) {

		forTable.replace(address, r);
	}

	public boolean contains(InetAddress address) {
		
		if (forTable.containsKey(address)){
			return true;
		} else {
			return false;
		}
	}
	
	public int getCost(InetAddress address) {
		
		if (forTable.containsKey(address)) {
			return forTable.get(address).cost;
		} else {
			return -1;
		}
	}
	
	public InetAddress getNextHop(InetAddress address) {
		if (forTable.containsKey(address)) {
			return forTable.get(address).nextHop;
		} else {
		
		return null;
		}
	}
	
	/**
	 * it encodes the forward table so that it can be sent in a DatagramPacket
	 * @return The table in a byte array
	 */
	public byte[] encode() {
		
		String toSend = "@@@@@;;;"; //this means that the message is routing table
    	
    	for (Map.Entry<InetAddress, Route> entry: forTable.entrySet()) {
    		String address = entry.toString();
    		String nextHop = entry.getValue().nextHop.toString();
    		String cost = Integer.toString(entry.getValue().cost);
    		
    		String row = address + ";" + nextHop + ";" + cost;
    		
    		toSend = toSend + row + ";;;";

    	}
    	
    	return toSend.getBytes();
	}
	
	/**
	 * It decodes a received byte array into a forward table
	 * @param bytes
	 * @return the forward table in a ForwardTable form
	 */
	public static ForwardTable decode(byte[] bytes) {
		
		Map<InetAddress, Route> table = new HashMap<>();
		
		String str = new String(bytes, 0, bytes.length);
        
		String[] splittedOnDouble = str.split(";;;");
		
		//we start from 1 because 0 is @@@@@
		for (int i = 1; i < splittedOnDouble.length; i++) {
						
			Route r = new Route();
			
			InetAddress address = null;
			
    		String[] element = splittedOnDouble[i].split(";");
    		
    		//check if element is a valid row of ForwTable
    		if (element.length == 3) {

        		r.cost = Integer.parseInt(element[2]);
        		try {
        			
        			/*
        			 * we do "substring" and "split" because the format we receive has some extra
        			 * characters
        			 */
    				r.nextHop = InetAddress.getByName(element[1].substring(1));	//next hop
    				
    				String[] tempAddress = element[0].substring(1).split("=");
    				
    				address = InetAddress.getByName(tempAddress[0]);
    				

    			} catch (UnknownHostException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}	
        		
        		table.put(address, r);
    		}
    		
    	
		}
		
		return new ForwardTable(table);	   
		
	}
	
	public Map<InetAddress, Route> getMap() {
		return this.forTable;
	}
}
