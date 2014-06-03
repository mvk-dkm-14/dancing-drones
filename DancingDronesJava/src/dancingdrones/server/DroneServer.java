package dancingdrones.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import dancingdrones.common.Protocol;
import dancingdrones.common.Settings;

/**
 * TODO INIT delen behöver förändras så att den utnyttjar den nya pakethanteringen
 * @author rodoo
 *
 */
public class DroneServer {
	// Initialize common buffers
	//private BufferedReader cIn;
	//private PrintWriter cOut;
	private InputStream cIn;
	private OutputStream cOut;
	private volatile LinkedList<byte[]> unread;
	//private List<byte[]> unread;
	private ArrayList<ARDroneController> drones;
	private static Logger logger = Logger.getLogger(DroneServer.class.getName());


	/**
	 * Creates a new DroneServerThread and runs it.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		Settings.printDebug("Creating server object");
		DroneServer s = new DroneServer();
		s.start();
	}
	
	protected void addPacket(byte[] packet) {
		unread.add(packet);
	}

	public void start() {
		Settings.printDebug("Setting up variables");
		unread = new LinkedList<byte[]>();
		drones = new ArrayList<ARDroneController>();	

		try {
			Settings.printInfo("Starting server");		
			Settings.printDebug("Creating listen socket");
			// Setting up a socket to handle the client connection on the default port
			ServerSocket listenSocket = new ServerSocket(Settings.LISTEN_PORT);

			Settings.printInfo("Waiting for client to connect..");
			// Wait for an incoming connection on the socket 
			Socket clientSocket = listenSocket.accept();
			// Someone connected
			Settings.printInfo("Client connected! IP: "+ clientSocket.getInetAddress());

			Settings.printDebug("Setting up socket streams");
			// Set up our desired buffers to the remote client, cIn(clientIn) and cOut
			cIn = clientSocket.getInputStream();
			cOut = clientSocket.getOutputStream();
			// These below are if we want to send strings instead of bytes
			//cIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream().));
			//cOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

			Settings.printDebug("Waiting for client INIT");
			// Wait for incoming data from the client
			byte[] cPacket = new byte[20];
			cIn.read(cPacket);
			Settings.printDebug("Recieved: " + cPacket[0]);
			// Check if it's an INIT request
			if(cPacket[0] == Protocol.Value.T_INIT + Protocol.Value.S_I_REQUEST) {
				// Right init message!
				Settings.printDebug("INIT REQUEST recieved");
				// Answer the client that we want to continue
				// Send everything in the buffer and clear it
				cOut.write(Protocol.Value.T_INIT + Protocol.Value.S_I_OK);
				cOut.flush();
			} else {
				Settings.printDebug("Non INIT recieved, terminating connection");
				// Send quit message to the client
				cOut.write(Protocol.Value.T_INIT + Protocol.Value.S_I_FAILED);
				cOut.flush();

				// Close the connection to the client
				clientSocket.close();
				Settings.printDebug("Client disconnected");
				Settings.printDebug("Closing all connections..");
				listenSocket.close();
				Settings.printDebug("Shutting down server..");
			}

			Settings.printDebug("Creating a listening thread and starts it.");
//			Thread listenThread = new Thread(new DroneServerListener(cIn, unread));
			Thread listenThread = new Thread(new DroneServerListener(cIn, this));
			listenThread.start();		// .start() creates a new thread, .run just runs the method

			//Main client loop
			Settings.printDebug("Main loop reached");
			boolean running = true;
			while(running){
				// Do we have any incoming packets from the client?
//				if(unread.size() < 1) {
//					//Settings.printDebug("Nothing to do, sleeping for 500ms");
//					Thread.sleep(33);
//				} else {
				if(unread.size() > 0) {
					// Packets exists! Fetch the oldest
					byte[] p = unread.pop();
					Settings.printDebug("Recieved packet from client, header: "+ (int)p[0] +" Size: "+p.length);

					byte[] h = Protocol.extractHeader(p[0]);
					switch(h[Protocol.Index.H_TYPE]){
					// Connect drone Packet.
					case(Protocol.Value.T_CONNECT):		// Connect type of packet.
						int id = p[Protocol.Index.DATA_OFFSET];
						Settings.printDebug("Connect Drone: "+ id);
						drones.add(new ARDroneController(id));
						break;
					case(Protocol.Value.T_CONTROL): // Protocol.v.C_SINGLE + Protocol.v.TESTFLIGHT)):
						switch(h[Protocol.Index.H_COMMAND]){
						case(Protocol.Value.C_MOVE):
							Settings.printInfo("Move: Not implemented yet..");
							break;
						case(Protocol.Value.C_TAKEOFF):
							Settings.printInfo("TakeOff!");
							drones.get(p[Protocol.Index.DATA_OFFSET]-1).takeOff();
							break;
						case(Protocol.Value.C_LAND):
							Settings.printInfo("Land!");
							drones.get(p[Protocol.Index.DATA_OFFSET]-1).land();
							break;
						case(Protocol.Value.C_EMERGENCY):
							Settings.printInfo("Sending emergency signal to drone!");
							drones.get(p[Protocol.Index.DATA_OFFSET]).sendEmergency();
							break;
						case(Protocol.Value.C_TESTFLIGHT):
							Settings.printDebug("Sending drone on test flight");
							Settings.printDebug("p[DATA_OFFSET]:"+ p[Protocol.Index.DATA_OFFSET]);
							//drones.get(p[Protocol.i.DATA_OFFSET]-1).testFlight();
							drones.get(p[Protocol.Index.DATA_OFFSET]-1).testFlight();
							break;						
						}
					break;
					case((byte)(Protocol.Value.T_QUIT)):
						Settings.printDebug("Client sent Quit");
					running = false;
					break;
					default:
						Settings.printDebug("unknown header recieved, value: "+p[0]);
					} // Switch
				} 
			}			
			Settings.printDebug("End of program reached, terminating");
			clientSocket.close();
			listenSocket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen thread to the server. So we're not affected by read blocks on sockets.
	 * @author rodoo
	 * 
	 * Make a method out of this and make this beutiful code instead?
	private void startUpdateLoop() {
        Thread thread = new Thread(new Runnable() 
        {
            @Override
            public void run()
            {
                updateLoop();
            }
        });
        thread.setName("ARDrone Control Loop");
        thread.start();
    }

	 *
	 */
	public class DroneServerListener implements Runnable {
		private static final int BUFFER_SIZE = 20;
		private final InputStream in;
		private DroneServer server;
		//		private final LinkedList<byte[]> unread;
		

//		public DroneServerListener(InputStream in, LinkedList<byte[]> unread) {
		public DroneServerListener(InputStream in, DroneServer server) {
			this.in = in;
			this.server = server;
//			this.unread = unread;
		}

		/**
		 * Listens after incoming traffic on the given socket until it receives 
		 * an interrupt.
		 */
		@Override
		public void run() {
			Settings.printDebug("ListenThread started.");

			byte[] b = new byte[BUFFER_SIZE];

			// Loop until we receive an interrupt
			while(!Thread.currentThread().isInterrupted()) {
				try {									
					// Read the first byte (header) and store it in the first index of b
					// Blocks until data is received.
					int bRead = in.read(b, 0, 1);
					if(bRead == 1){			
						// Packet received!
						Settings.printDebug("Something received!");
						// From the header get how big the packet should be.
						int pSize = Protocol.getPacketSize(b[0]);
						if(pSize > -1){
							// Valid packet received
							byte[] p = new byte[pSize];
							//packet[0] = b[0];
							// Read the rest of the packet into the buffer
							bRead = in.read(b, 1, pSize-1);
							Settings.printDebug("bytesRead (2nd):"+ bRead);
							if(bRead == pSize-1){
								// Correct number of bytes read!
								// Copy to new array to store in unread
								for(int i=0; i<pSize; i++)
									p[i] = b[i];
//								unread.add(p);
								server.addPacket(p);
								Settings.printDebug("Added packet with header: "+ p[0]);
							} else
								Settings.printDebug("Error reading data, wrong number bytes read.");
						} else
							Settings.printDebug("Unknown header received: "+ b[0]);
					} else 
						Settings.printDebug("Nothing to read.. (or EOF??)");			
				} catch (IOException e) {
					// Something happened to our InputStream, probably socket closed.
					// Signal to the thread to stop (is this needed?)
					Thread.currentThread().interrupt();
					Settings.printDebug("ListenThread died with the following exception:");
					e.printStackTrace();
				}
			}
			Settings.printDebug("ListenThread stopped.");
		}
	}
}