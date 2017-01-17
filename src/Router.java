/**
 * @author Raza Qazi
 * @version 1.0
 * @id 10134926
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import cpsc441.a4.shared.*;


/**
 * Router Class
 * 
 * This class implements the functionality of a router
 * when running the distance vector routing algorithm.
 * 
 * The operation of the router is as follows:
 * 1. send/receive HELLO message
 * 2. while (!QUIT)
 *      receive ROUTE messages
 *      update mincost/nexthop/etc
 * 3. Cleanup and return
 * 
 * A separate process broadcasts routing update messages
 * to directly connected neighbors at regular intervals.
 * 
 *      
 * @author 	Majid Ghaderi
 * @version	2.1
 *
 */
public class Router {
    private int[] linkcost; // linkcost[i] is the cost of link to router
    private int[] nexthop; // nexthop[i] is the next hop mode to reach router
    private int[][] mincost; // mincost[i] is the mincost vector of router

    private Socket socket;
    private DvrPacket dvr;
    private Timer timer;

    private int routerId;
    private String serverName;
    private int serverPort;
    private int updateInterval;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * Constructor to initialize the router instance
     *
     * @param routerId       Unique ID of the router starting at 0
     * @param serverName     Name of the host running the network server
     * @param serverPort     TCP port number of the network server
     * @param updateInterval Time interval for sending routing updates to neighboring routers (in milli-seconds)
     */
    public Router(int routerId, String serverName, int serverPort, int updateInterval) {
        // to be completed
        this.routerId = routerId;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.updateInterval = updateInterval;
        this.timer = new Timer(true);
    }


    /**
     * starts the router
     *
     * @return The forwarding table of the router
     */
    public RtnTable start() {
        try {
            // Open tcp connection to server
            socket = new Socket(serverName, serverPort);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // Setup global variables...
            this.out = out;
            this.in = in;

            // send receive process HELLO
            DvrPacket helloPacket = new DvrPacket(routerId, DvrPacket.SERVER, DvrPacket.HELLO);
            out.writeObject(helloPacket);

            dvr = (DvrPacket) in.readObject();

            initializeTables(dvr);

            // timer.start @ fixed interval
            this.timer.scheduleAtFixedRate(new TimeoutHandler(this), updateInterval, updateInterval);

            // while NOT QUIT PACKET // do
            while (true) {
                dvr = (DvrPacket) in.readObject();
                // Quit when end packet is received
                if (dvr.toString().contains("Quit"))
                    break;
                else if (dvr.sourceid == DvrPacket.SERVER)
                    initializeTables(dvr);
                else
                    processDvr(dvr);
            }

            // Shutdown all processes before returning new table
            timer.cancel();

            socket.close();

            out.close();
            in.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // return routing table
        return new RtnTable(mincost[routerId], nexthop);
    }

    /**
     * processes a received packet from server.
     * and depending on the update, updates mincost[][]
     * for all routers in the network and reconfigures
     * nexthop[]
     *
     * @param dvr Packet recevied from server processed
     */
    public synchronized void processDvr(DvrPacket dvr) {
        // if dvr.sourceid == dvrpacket.SERVER
        if (dvr.toString().contains("SERVER")) {
            // System.out.println(dvr.toString());
            // update link cost vector AND update min cost vector
            mincost[routerId] = linkcost = dvr.getMinCost();
            initializeNextHop();
        } else {
            // this is the regular routing update from a neighbour
            // update min cost vector
            mincost[dvr.sourceid] = dvr.getMinCost();

            // Run bellman ford algorithm: Update mincost table
            // Process each node one by one
            for (int i = 0; i < linkcost.length; i++) {
                // check associated value in mincost for this particular router
                int min = mincost[routerId][i];
                int tempHop = nexthop[i];
                for (int j = 0; j < linkcost.length; j++) {
                    // For each adjacent node, check the cost to get to
                    // desired node
                    int newCost = linkcost[j] + mincost[j][i];
                    // If new cost is less than current min
                        // Replace
                    if (newCost < min) {
                        min = newCost;
                        tempHop = j;
                    }
                }
                // Update mincost and nextHop
                mincost[routerId][i] = min;
                nexthop[i] = tempHop;
            }
        }
    }

    /**
     * nextHop configured based on type of
     * link cost data.
     */
    private void initializeNextHop() {
        // Initialize nextHop to updated values
        for (int i = 0; i < linkcost.length; i++) {
            if (linkcost[i] == DvrPacket.INFINITY)
                nexthop[i] = -1;
            else if (linkcost[i] == 0)
                nexthop[i] = routerId;
            else
                nexthop[i] = i;
        }
    }

    /**
     * Initalizes mincost[][], linkcost[] and nextHop[]
     * based on values provided by server
     *
     * @param dvr Uses data from server DvrPacket
     *            to initialize internal data structures
     */
    private void initializeTables(DvrPacket dvr) {
        // cost of link to router initialized
        linkcost = dvr.getMinCost();
        mincost = new int[linkcost.length][linkcost.length];
        nexthop = new int[linkcost.length];

        // Initialize all values of mincost to INFINITY
        for (int i = 0; i < linkcost.length; i++)
            Arrays.fill(mincost[i], DvrPacket.INFINITY);

        // Setup min cost for current router to linkcost array.
        mincost[routerId] = linkcost;

        // Setup nextHop
        initializeNextHop();
    }

    /**
     * Sends out periodic update to other routers on mincost for running router
     *
     * @throws IOException
     */
    public synchronized void sendDistanceVector() throws IOException {
        // Sends out mincost distance vector for each directly connected router...
        for (int i = 0; i < linkcost.length; i++) {
            // Only send out for those that are not infinite or not from originating router
            if (!(i == routerId || linkcost[i] == DvrPacket.INFINITY)) {
                // Send out mincost for all directly adjacent routers.
                DvrPacket dvr = new DvrPacket(routerId, i, DvrPacket.ROUTE, mincost[routerId]);
                out.writeObject(dvr);
            }
        }
    }

    /**
     * A simple test driver
     */
    public static void main(String[] args) {
        // default parameters
        int routerId = 0;
        String serverName = "localhost";
        int serverPort = 2227;
        int updateInterval = 1000; //milli-seconds

        // the router can be run with:
        // i. a single argument: router Id
        // ii. all required arguments
        if (args.length == 1) {
            routerId = Integer.parseInt(args[0]);
        } else if (args.length == 4) {
            routerId = Integer.parseInt(args[0]);
            serverName = args[1];
            serverPort = Integer.parseInt(args[2]);
            updateInterval = Integer.parseInt(args[3]);
        } else {
            System.out.println("incorrect usage, try again.");
            System.exit(0);
        }

        // print the parameters
        System.out.printf("starting Router #%d with parameters:\n", routerId);
        System.out.printf("Relay server host name: %s\n", serverName);
        System.out.printf("Relay server port number: %d\n", serverPort);
        System.out.printf("Routing update interval: %d (milli-seconds)\n", updateInterval);

        // start the server
        // the start() method blocks until the router receives a QUIT message
        Router router = new Router(routerId, serverName, serverPort, updateInterval);
        RtnTable rtn = router.start();
        System.out.println("Router terminated normally");

        // print the computed routing table
        System.out.println();
        System.out.println("Routing Table at Router #" + routerId);
        System.out.print(rtn.toString());
    }

}

