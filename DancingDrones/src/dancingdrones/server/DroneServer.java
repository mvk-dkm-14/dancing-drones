package dancingdrones.server;
import java.io.IOException;

/**
 * Oklart om denna egentligen beh�vs eller om den ska heta
 * s�h�r, �r enbart till f�r att starta upp en instans av
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
