package dancingdrones.debugClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import dancingdrones.common.Protocol;
import dancingdrones.common.Settings;


public class DebugConnection {
	private Socket server;
	private InputStream sIn;
	private OutputStream sOut;
	private byte[] recieved = new byte[19];
//	private BufferedReader sIn;
//	private PrintWriter sOut;
	
	public void connect(String ipAdress, int port) throws UnknownHostException, IOException{
		System.out.println("[INFO] Connecting to " + ipAdress +":"+ port);
		server = new Socket("localhost",1337);
		System.out.println("[INFO] Connected!");
//		sIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
//		sOut = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
		sIn = server.getInputStream();
		sOut = server.getOutputStream();
		sOut.write(Protocol.initRequest());
		sOut.flush();
		int bytesRead = sIn.read(recieved);
		System.out.println("[DEBUG] Packet recieved. Size: " + bytesRead + " Header: "+ recieved[0]);
		boolean success = false;
		switch(recieved[0]){
			case Protocol.var.INIT + Protocol.var.I_OK:
				System.out.println("[INFO] INIT OK");
				success = true;
				break;
			case Protocol.var.INIT + Protocol.var.I_FAILED:
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
		
		boolean running = true;
		while(running) {
			
		}
		System.out.println("[DEBUG] End of connect");
	}
	
	public void init(){
		
	}
	
	public void run(){
		boolean running = true;
		while(running){
		}
	}
	
}
