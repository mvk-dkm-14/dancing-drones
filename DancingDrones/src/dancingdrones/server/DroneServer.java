package dancingdrones.server;
import java.io.IOException;

/**
 * Oklart om denna egentligen behövs eller om den ska heta
 * såhär, är enbart till för att starta upp en instans av
 * DroneServer.
 * @author Rodoo
 *
 */
public class DroneServer {

	public static void main(String[] args) throws IOException{
		// DroneServerThread server = new DroneServerThread();
		new DroneServerThread();
	}
}
