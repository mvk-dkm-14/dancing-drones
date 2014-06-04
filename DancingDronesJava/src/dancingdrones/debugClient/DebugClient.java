package dancingdrones.debugClient;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import dancingdrones.common.Protocol;
import dancingdrones.common.Settings;


public class DebugClient {
	private Socket server;
	private InputStream sIn;
	private OutputStream sOut;
	private byte[] recieved = new byte[19];
	//private BufferedReader sIn;
	//private PrintWriter sOut;

	private class KeyEvent implements KeyListener {

		@Override
		public void keyPressed(java.awt.event.KeyEvent arg0) {
			Settings.printDebug("Key Pressed! arg0:"+ arg0.getKeyChar());
			
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent arg0) {
			Settings.printDebug("Key Pressed! arg0:"+ arg0.getKeyChar());
			
		}

		@Override
		public void keyTyped(java.awt.event.KeyEvent arg0) {
			Settings.printDebug("Key Pressed! arg0:"+ arg0.getKeyChar());
			
		}
		
	}
	//public DroneClient(){}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		if(args.length != 2) {
			System.out.println("Usage: java DroneClient <ip> <port>");
			System.exit(1);
		}
		DebugClient dc = new DebugClient();
		dc.connect(args[0], Integer.parseInt(args[1]));
		System.out.println("Connection closed, exiting..");
	}
	
	public void connect(String ipAdress, int port) throws UnknownHostException, IOException{
		System.out.println("[INFO] Connecting to " + ipAdress +":"+ port);
		server = new Socket("localhost",1337);
		System.out.println("[INFO] Connected!");
//		sIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
//		sOut = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
		sIn = server.getInputStream();
		sOut = server.getOutputStream();
		sOut.write(Protocol.sendInitRequest());
		sOut.flush();
		int bytesRead = sIn.read(recieved);
		System.out.println("[DEBUG] Packet recieved. Size: " + bytesRead + " Header: "+ recieved[0]);
		boolean success = false;
		switch(recieved[0]){
			case Protocol.Value.T_INIT + Protocol.Value.S_I_OK:
				System.out.println("[INFO] INIT OK");
				success = true;
				break;
			case Protocol.Value.T_INIT + Protocol.Value.S_I_FAILED:
				System.out.println("[INFO] INIT Failed");
				break;
			default:
				System.out.println("[INFO] INIT Unknown response");
				break;
		}
		if(!success) {
			Settings.printDebug("INIT Failed, exiting..");
			System.exit(1);
		}
		
		Scanner s = new Scanner(System.in);
		boolean running = true;
		while(running) {
			printMenu();
			String o = s.nextLine();
			byte[] c = o.getBytes("US-ASCII");
			System.out.println("o: "+ o);
			switch(c[0]){
			case 'c':
				sOut.write(Protocol.sendConnectDrone(1));
				break;
			case 'f':
				Settings.printDebug("Sending testFlight id 1 to server..");
				sOut.write(Protocol.sendTestFlightDrone(1));
				break;
			case 'q':
				sOut.write(Protocol.sendQuit());
				running = false;
				break;
			case 'e':
				sOut.write(Protocol.sendEmergency(1));
				break;
			case 't':
				sOut.write(Protocol.sendTakeOff(1));
				break;
			case 'l':
				sOut.write(Protocol.sendLand(1));
				break;
			case 'h':
				System.out.println("Höjd? (mm): ");
				int height = Integer.parseInt(s.nextLine());
				System.out.println(height);
				sOut.write(Protocol.sendTargetHeight(1, height));
				break;
			default:
				break;
			}
			sOut.flush();
		}
		System.out.println("[DEBUG] End of connect");
		s.close();
	}
	
	private void printMenu(){
		System.out.println("[C]onnect Drone \t[T]ake Off \t[L]and \tTest[F]light \tSet [H]eight");
	}
}
