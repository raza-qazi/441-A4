package cpsc441.a4.shared;

import java.util.*;
 
/**
 * Class RtnTable
 * 
 * This is a wrapper class for the forwarding table at roiuters.
 * 
 * @author 	Majid Ghaderi
 * @version	1.1
 *
 */
public class RtnTable {
	private int[] mincost;	// mincost vector
	private int[] nexthop;	// next hop vector
	
	
	/**
	 * No-argumenrt constructor
	 */
	public RtnTable() {
		this(new int[0], new int[0]);
	}
	
	
	/**
	 * Constructor
	 * 
	 * Initializes mincost and nexthop with the given parameter values.
	 */
	public RtnTable(int[] mincost, int[] nexthop) {
		if (mincost.length != nexthop.length)
			throw new IllegalArgumentException("Arrays have different lengthes");
		
		this.mincost = Arrays.copyOf(mincost, mincost.length);
		this.nexthop = Arrays.copyOf(nexthop, nexthop.length);
	}
	
	
	/**
	 * Returns a new copy of the mincost array.
	 */
	public int[] getMinCost() {
		return Arrays.copyOf(mincost, mincost.length);
	}
	
	
	/**
	 * Returns a new copy of the nexthop array.
	 */
	public int[] getNextHop() {
		return Arrays.copyOf(nexthop, nexthop.length);
	}
	
	
	/**
	 * Returns a string representation of the forwarding table.
	 */
	public String toString() {
		String table = "";
		
		table += "-------------------------\n";
		
		for (int i = 0; i < mincost.length; i++) 
			table += String.format("  mincost[%d] = %d via %d\n", i, mincost[i], nexthop[i]);
		
		table += "-------------------------\n";
		
		return table;
	}
	
	
    /**
     * A simple test driver
     * 
     */
	public static void main(String[] args) {
		int[] mincost = {10, 20, 30};
		int[] nexthop = {1, 2, 1};
		
		RtnTable table = new RtnTable(mincost, nexthop);
		
		System.out.println("Routing Table at Router #0");
		System.out.println(table.toString());
	}

}
