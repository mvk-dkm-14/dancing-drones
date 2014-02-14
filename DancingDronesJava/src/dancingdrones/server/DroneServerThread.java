package dancingdrones.server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import dancingdrones.common.Protocol;
import dancingdrones.common.Settings;


public class DroneServerThread implements Runnable {
	// Initialize common buffers
	//private BufferedReader cIn;
	//private PrintWriter cOut;
	private InputStream cIn;
	private OutputStream cOut;
	private byte[]	cPacket;
	
	/**
	 * Main function of the server
	 * Starts by listening after a client.
	 * After that it is up to the client to connect drones.
	 * @throws IOException
	 */
	public DroneServerThread() throws IOException {
		Settings.printInfo("Starting server");
		Settings.printDebug("Setting up variables");
		cPacket = new byte[21];
		
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
		//cPacket = cIn.readLine();
		cIn.read(cPacket);
		Settings.printDebug("Recieved: " + cPacket[0]);
		// Check if it's an INIT request
		if(cPacket[0] == Protocol.var.INIT + Protocol.var.I_REQUEST) {
//		if(	Protocol.getPacketType(cPacket) == Protocol.var.INIT &&
//			Protocol.getSelection(cPacket) == Protocol.var.REQUEST) {
			// Right init message!
			Settings.printDebug("INIT REQUEST recieved");
			// Answer the client that we want to continue
			//cOut.write(Protocol.initResponse(true));
			cOut.write(Protocol.var.INIT + Protocol.var.I_OK);
			// Send everything in the buffer and clear it
			cOut.flush();
		} else {
			Settings.printDebug("Non INIT recieved, terminating connection");
			// Send quit message to the client
//			cOut.write(Protocol.initResponse(false));
			cOut.write(Protocol.var.INIT + Protocol.var.I_FAILED);
			cOut.flush();
			// Close the connection to the client
			clientSocket.close();
			Settings.printDebug("Client disconnected");
			Settings.printDebug("Closing all connections..");
			listenSocket.close();
			Settings.printDebug("Shutting down server..");
		}
		
		//Main client loop
		boolean running = true;
		while(running){
			Settings.printDebug("Main loop reached");
			
			cOut.write(Protocol.sendQuit());
			running = false;
		}
		
		Settings.printDebug("End of program reached, terminating");
		clientSocket.close();
		listenSocket.close();
		
		
	}

	@Override
	public void run() {

	}
	
	

}
