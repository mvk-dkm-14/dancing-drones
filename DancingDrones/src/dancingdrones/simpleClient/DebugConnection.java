package dancingdrones.simpleClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class DebugConnection {
	private Socket server;
	private BufferedReader sIn;
	private PrintWriter sOut;
	
	public void connect(String ipAdress, int port) throws UnknownHostException, IOException{
		System.out.println("[INFO] Connecting to " + ipAdress +":"+ port);
		server = new Socket("localhost",1337);
		System.out.println("[INFO] Connected!");
		sIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
		sOut = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
		sOut.println("I");
		sOut.flush();
		String recieved = sIn.readLine();
		System.out.println("[DEBUG] Recieved: "+ recieved);
		if(recieved.equals("I_OK")){
			System.out.println("[INFO] INIT OK");
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
