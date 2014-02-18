package dancingdrones.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

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
	private LinkedList<byte[]> unread;
	//private List<byte[]> unread;
	private ArrayList<ARDroneController> drones;
		
	
	/**
	 * Creates a new DroneServerThread and runs it.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		Settings.printDebug("Creating server object");
		DroneServer s = new DroneServer();
		s.start();
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
			if(cPacket[0] == Protocol.v.T_INIT + Protocol.v.S_I_REQUEST) {
				// Right init message!
				Settings.printDebug("INIT REQUEST recieved");
				// Answer the client that we want to continue
				// Send everything in the buffer and clear it
				cOut.write(Protocol.v.T_INIT + Protocol.v.S_I_OK);
				cOut.flush();
			} else {
				Settings.printDebug("Non INIT recieved, terminating connection");
				// Send quit message to the client
				cOut.write(Protocol.v.T_INIT + Protocol.v.S_I_FAILED);
				cOut.flush();
				
				// Close the connection to the client
				clientSocket.close();
				Settings.printDebug("Client disconnected");
				Settings.printDebug("Closing all connections..");
				listenSocket.close();
				Settings.printDebug("Shutting down server..");
			}
			
			Settings.printDebug("Creating a listening thread and starts it.");
			Thread listenThread = new Thread(new DroneServerListener(cIn, unread));
			listenThread.start();		// .start() creates a new thread, .run just runs the method
			
			//Main client loop
			Settings.printDebug("Main loop reached");
			boolean running = true;
			while(running){
				// Do we have any incomming packets from the client?
				if(unread.size() < 1) {
					//Settings.printDebug("Nothing to do, sleeping for 500ms");
					Thread.sleep(33);
				} else {
					// Packets exists! Fetch the oldest
					byte[] p = unread.pop();
					Settings.printDebug("Recieved packet from client, header: "+ (int)p[0] +" Size: "+p.length);
					
					byte[] h = Protocol.extractHeader(p[0]);
					switch(h[Protocol.i.H_TYPE]){
					// Connect drone Packet.
					case(Protocol.v.T_CONNECT):		// Connect type of packet.
						int id = p[Protocol.i.DATA_OFFSET];
						Settings.printDebug("Connect Drone: "+ id);
						drones.add(new ARDroneController(id));
						break;
					case(Protocol.v.T_CONTROL): // Protocol.v.C_SINGLE + Protocol.v.TESTFLIGHT)):
						switch(h[Protocol.i.H_COMMAND]){
						case(Protocol.v.C_MOVE):
							Settings.printInfo("Move: Not implemented yet..");
							break;
						case(Protocol.v.C_TAKEOFF):
							Settings.printInfo("TakeOff!");
							drones.get(p[Protocol.i.DATA_OFFSET]-1).takeOff();
							break;
						case(Protocol.v.C_LAND):
							Settings.printInfo("Land!");
							drones.get(p[Protocol.i.DATA_OFFSET]-1).land();
							break;
						case(Protocol.v.C_EMERGENCY):
							Settings.printInfo("Sending emergency signal to drone!");
							drones.get(p[Protocol.i.DATA_OFFSET]).sendEmergency();
							break;
						case(Protocol.v.C_TESTFLIGHT):
							Settings.printDebug("Sending drone on test flight");
							Settings.printDebug("p[DATA_OFFSET]:"+ p[Protocol.i.DATA_OFFSET]);
							//drones.get(p[Protocol.i.DATA_OFFSET]-1).testFlight();
							drones.get(p[Protocol.i.DATA_OFFSET]-1).testFlight();
							break;						
						}
						break;
					case((byte)(Protocol.v.T_QUIT)):
						Settings.printDebug("Client sent Quit");
						running = false;
						break;
					default:
						Settings.printDebug("unknown header recieved, value: "+p[0]);
					}
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
	 */
	public class DroneServerListener implements Runnable {
		private InputStream in;
		private LinkedList<byte[]> unread;
		
		public DroneServerListener(InputStream in, LinkedList<byte[]> unread) {
			this.in = in;
			this.unread = unread;
		}

		@Override
		public void run() {
			Settings.printDebug("ListenThread started.");
			try {
				while(true) {
					byte[] b = new byte[20];
					
					int bytesRead = in.read(b, 0, 1);
					if(bytesRead == 1){							// if packet received
						Settings.printDebug("Something received?");
						int packetSize = Protocol.getPacketSize(b[0]);
						if(packetSize > -1){
							byte[] packet = new byte[packetSize];
							packet[0] = b[0];
							bytesRead = in.read(b, 0, packetSize-1);
							Settings.printDebug("bytesRead (2nd):"+ bytesRead);
							if(bytesRead == packetSize-1){
								for(int i=1; i<packetSize; i++){
									packet[i] = b[i-1];
								}
							} else
								Settings.printDebug("Error reading data, wrong number bytes read.");
							Settings.printDebug("Added packet with header: "+ packet[0]);
							unread.add(packet);
						} else 
							Settings.printDebug("Unknown header received: "+ b[0]);
					} else 
						Settings.printDebug("Nothing to read..");
				}			
			} catch (IOException e) {
				Settings.printDebug("ListenThread died with the following exception:");
				e.printStackTrace();
			}
		}		
	}
}
