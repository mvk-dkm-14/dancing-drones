package dancingdrones.simpleClient;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class DroneClient {;
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
		DebugConnection server = new DebugConnection();
		server.connect(args[0], Integer.parseInt(args[1]));
		System.out.println("Connection closed, exiting..");
	}
}
