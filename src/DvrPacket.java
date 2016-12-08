package cpsc441.a4.shared;

import java.util.*;
import java.io.*;

 
/**
 * Class DvrPakcet
 * 
 * DvrPacket defines the structure of the messages used to 
 * exchange routing information between the routers.
 *
 * Each packet has mincost array and specifies the sender 
 * and receiver of the packet. The mincost array is dynamically created
 * based on the number of routers in the network. 
 *
 * The class is serializable so that it can be used
 * with Java object IO streams for reading and writing
 * from and to a TCP socket.
 * 
 * @author 	Majid Ghaderi
 * @version	2.1
 *
 */
public class DvrPacket implements Serializable {
	// constants used in the program
    public static final int SERVER = 100;		// network server ID
    public static final int INFINITY = 999;		// infinity link cost
    
	// type of the DVR packet 
	public static final int HELLO = 1;	// used for hand shake between routers and net server
	public static final int QUIT = 2;	// used to signal termination to routers
	public static final int ROUTE = 3;	// used to denote regular routing packets 

    // routing information
	public int  sourceid;	// ID of the router sending this packet
	public int  destid;		// ID of the router to which the packet is being sent
	public int  type;		// type of the packet
	public int[] mincost;	// min cost vector at the router

	
	/**
	 * Default constructor
	 * 
	 * Creates an empty packet of type ROUTE
	 * and no min cost information.
	 */
	public DvrPacket() {
		this(SERVER, SERVER, ROUTE, new int[0]);
	}
	
	/**
	 * Constructor
	 * 
	 * Creates a packet that does not include any
	 * min cost information, but is initialized with given IDs and type.
	 */
	public DvrPacket(int sourceid, int destid, int type) {
		this(sourceid, destid, type, new int[0]);
	}

	
	/**
	 * Constructor
	 * 
	 * Creates a packet that includes a
	 * min cost vector initialized with mincost.
	 */
	public DvrPacket(int sourceid, int destid, int type, int[] mincost) {
		this.sourceid = sourceid;
		this.destid = destid;
		this.type = type;
		setMinCost(mincost);
	}
	
	
	/**
	 * Copy constructor
	 * 
	 */
	public DvrPacket(DvrPacket dvr) {
		this(dvr.sourceid, dvr.destid, dvr.type, dvr.mincost);
	}

	/**
	 * Returns a new copy of mincost array.
	 * 
	 */
	public int[] getMinCost() {
		return Arrays.copyOf(mincost, mincost.length);
	}
	
	/**
	 * Sets the mincost array with a copy of the parameter mc.
	 * 
	 */
	public void setMinCost(int[] mc) {
		this.mincost = Arrays.copyOf(mc, mc.length);
	}
	
	/**
	 * Returns a String representation of the packet.
	 */
	public String toString() {
		return String.format("%s (%d->%d) %s", getTypeStr(), sourceid, destid, Arrays.toString(mincost));
	}

	// Converts packet type to a string
	private String getTypeStr() {
		switch (type) {
		case HELLO: 
			return "Hello";
		case ROUTE:
			return "Route";
		case QUIT:
			return "Quit ";
		default:
			return "Unknown";
		}
	}

	
    /**
     * A simple test driver
     * 
     */
	public static void main(String[] args) {
		DvrPacket dvr1 = new DvrPacket(SERVER, SERVER, QUIT);
		System.out.println("packet#1 = " + dvr1);
		
		int[] mincost = {1, 2, 3, 4, 5};
		
		DvrPacket dvr2 = new DvrPacket(1, 2, ROUTE, mincost);
		System.out.println("packet#2 = " + dvr2);
		
		mincost = dvr2.getMinCost();
		mincost[0] += 10;
		
		dvr2.setMinCost(mincost);
		System.out.println("packet#3 = " + dvr2);
	}

}
