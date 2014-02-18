package dancingdrones.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.codeminders.ardrone.ARDrone;

import dancingdrones.common.Settings;

public class ARDroneController {
	private ARDrone drone;
	private int id;
	private float targetHeight;
	private int group;
	
	private int CONNECT_TIMEOUT = 5000;
	
	public ARDroneController(int id) throws UnknownHostException{
		Settings.printDebug("Creating drone with ip ."+ id);
		this.id = id;
		InetAddress ip = InetAddress.getByName("192.168.1." + (id));
		drone = new ARDrone(ip);
	}
	
	/**
	 * Connects to the drone, clear emergency signal, 
	 * trims it and take off!
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void takeOff() throws IOException, InterruptedException{
		// Create ARDrone object,
		// connect to drone and initialize it.
		drone.connect();
		drone.clearEmergencySignal();
			
		// Wait until drone is ready
		drone.waitForReady(CONNECT_TIMEOUT);
		
		// do TRIM operation
		drone.trim();
		
		// Take off
		System.err.println("Taking off");
		drone.takeOff();
		Thread.sleep(5000);
	}
	
	/**
	 * Lands the drone and disconnects from it.
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void land() throws InterruptedException, IOException{
		System.err.println("Landing");
		drone.land();
		
		// Give it some time to land
		Thread.sleep(2000);
		
		// Disconnect from the done
		drone.disconnect();
	}
	
	public void sendEmergency() throws IOException {
		drone.sendEmergencySignal();
	}
	
	/**
	 * 
	 * @param left_right_tilt
	 * @param front_back_tilt
	 * @param vertical_speed
	 * @param angular_speed
	 * @throws IOException
	 */
	public void move(	float left_right_tilt, float front_back_tilt, 
						float vertical_speed, float angular_speed) throws IOException{
		drone.move(left_right_tilt, front_back_tilt, vertical_speed, angular_speed);
	}
	
	public void testFlight() throws IOException, InterruptedException {
		// Create ARDrone object,
		// connect to drone and initialize it.
		// drone = new ARDrone();
		drone.connect();
		drone.clearEmergencySignal();
		
		// Wait until drone is ready
		drone.waitForReady(CONNECT_TIMEOUT);
		
		// do TRIM operation
		drone.trim();
		
		// Take off
		System.err.println("Taking off");
		drone.takeOff();
		
		// Fly a little :)
		Thread.sleep(5000);
		
		// Land
		System.err.println("Landing");
		drone.land();
		
		// Give it some time to land
		Thread.sleep(2000);
		
		// Disconnect from the done
		drone.disconnect();
    }
}
