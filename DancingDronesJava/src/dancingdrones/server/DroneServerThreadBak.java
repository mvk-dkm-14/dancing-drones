package dancingdrones.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import dancingdrones.common.Protocol;
import dancingdrones.common.Settings;


public class DroneServerThreadBak implements Runnable {
	// Initialize common buffers
	private BufferedReader cIn;
	private PrintWriter cOut;
	private String	cPacket;
	
	/**
	 * Main function of the server
	 * Starts by listening after a client.
	 * After that it is up to the client to connect drones.
	 * @throws IOException
	 */
	public DroneServerThreadBak() throws IOException {
		Settings.printInfo("Starting server");
		Settings.printDebug("Setting up variables");
		
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
		cIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		cOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				
		Settings.printDebug("Waiting for client INIT");
		// Wait for incoming data from the client
		cPacket = cIn.readLine();
		
		// Check if it's an INIT request
		if(cPacket.equals(Protocol.INIT)) {
			// Right init message!
			Settings.printDebug("INIT recieved");
			// Answer the client that we want to continue
			cOut.println("IOK");
			// Send everything in the buffer and clear it
			cOut.flush();
		} else {
			Settings.printDebug("Non INIT recieved, terminating connection");
			// Send quit message to the client
			cOut.println(Protocol.QUIT);
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
			// Do we have any incoming data?
			if(cIn.ready()){
				
			}
		}
		
		Settings.printDebug("End of program reached, terminating");
		clientSocket.close();
		listenSocket.close();
		
		
	}

	@Override
	public void run() {

	}
	
	

}
