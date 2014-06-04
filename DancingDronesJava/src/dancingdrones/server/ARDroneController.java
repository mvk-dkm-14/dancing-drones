package dancingdrones.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneStatusChangeListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

import dancingdrones.common.Settings;

public class ARDroneController implements NavDataListener, DroneStatusChangeListener {
	private long nextOut = 0;
	
	private boolean ready = false;
	private boolean flying = false;
	private int battery = 0;
	private boolean running = false;
	private long lastCommandSentAt = 0;
	
	private LinkedList<float[]> moveQueue = new LinkedList<float[]>();
	
	private int lastSequence=0;
	
	//private AtomicReference<ARDrone> drone;
	private ARDrone drone;
	private NavData nd;
	private int id;
	private float targetHeight;
	private int group;
	

	
	public ARDroneController(int id) throws IOException, InterruptedException {
		Settings.printDebug("[ARDroneController] Creating drone with ip ."+ id);
		this.id = id;
		drone = new ARDrone(InetAddress.getByName(Settings.IP_NET + id));
		
		// Tell the drone to send updates to this instance (object)
		drone.addStatusChangeListener(this);
		drone.addNavDataListener(this);
//		startUpdateLoop();
		initDrone();
	}
	
	public void initDrone() throws IOException, InterruptedException{
		targetHeight = 1500;
		System.err.println("[ARDroneController][ID"+id+"] Connecting to the drone");
		drone.connect();
		drone.waitForReady(Settings.CONNECT_TIMEOUT);
		drone.clearEmergencySignal();
		drone.trim();
		System.err.println("[ARDroneController][ID"+id+"] Connected to the drone");
		drone.setConfigOption("CONTROL:altitude_max", "2000");
		drone.setConfigOption("CONTROL:altitude_min", "1000");
		System.err.println("[ARDroneController][ID"+id+"] Sent max/min altitude to Drone");
		lastCommandSentAt = System.currentTimeMillis();
	}
	
	/**
	 * Connects to the drone, clear emergency signal, 
	 * trims it and take off!
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void takeOff() throws IOException, InterruptedException{
		// do TRIM operation, calibrate default horizontal plane
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
		float[] m = {left_right_tilt, front_back_tilt, vertical_speed, angular_speed};
		moveQueue.add(m);
	}
	
	
	public void testFlight() throws IOException, InterruptedException {
		// Create ARDrone object,
		// connect to drone and initialize it.
		// drone = new ARDrone();
//		drone.connect();
		drone.clearEmergencySignal();
		Thread.sleep(1000);
		
		// Wait until drone is ready
		drone.waitForReady(Settings.CONNECT_TIMEOUT);
		Thread.sleep(1000);
		
		// do TRIM operation
		drone.trim();
		Thread.sleep(1000);
		
		
		// Take off
		System.err.println("Taking off");
		drone.takeOff();
		Thread.sleep(1000);
		
		// Fly a little :)
		Thread.sleep(5000);
		
		// Land
		System.err.println("Landing");
		drone.land();
		Thread.sleep(1000);
		
		// Give it some time to land
		Thread.sleep(2000);
		
		// Disconnect from the done
		drone.disconnect();
    }
	
	 /**
     * This method is called whenever the drone changes from BOOTSTRAP or
     * ERROR modes to DEMO mode. Could be used for user-supplied initialization
     */
	@Override
	public void ready() {
		this.ready = true;
	}
	
	public void setTargetHeight(int targetHeight) {
		Settings.printDebug("set target height called with percentage: " + targetHeight);
		if (targetHeight < 101) {
			targetHeight = targetHeight * 20;
			targetHeight += 500;
		}
		this.targetHeight = targetHeight;
		Settings.printDebug("set target height actual to: " + targetHeight);
		
	}
	
	public void moveToTargetHeight() {
		try {
			if(Math.abs(nd.getAltitude()*1000-targetHeight) < 200)
				hover();	
			else if(nd.getAltitude()*1000 < targetHeight)
				ascend();
			else if(nd.getAltitude()*1000 > targetHeight)
				descend();
		} catch(IOException e) {
			System.out.println("Something went wrong when sending commands to drone!");
			e.printStackTrace();
		}
	}
	
	public void hover() throws IOException {
		drone.hover();
	}
	
	public void ascend() throws IOException{
		drone.move(0, 0, 1, 0);
	}
	
	public void descend() throws IOException {
		drone.move(0, 0, -1, 0);
	}

	@Override
	public void navDataReceived(NavData nd) {
		if(nd.getSequence() < lastSequence) {
			// Old data received
			Settings.printDebug("[ARDroneController] Received old navdata");
			return;
		}
		this.nd = nd;
		// Fresh data received
		this.flying = nd.isFlying();
		
		if(System.currentTimeMillis()>nextOut) {
			Settings.printDebug("Altitude: "+nd.getAltitude()+"m");
			Settings.printDebug("Battery level: "+nd.getBattery()+"%");
			nextOut = System.currentTimeMillis()+1000;
		}
		
		lastSequence = nd.getSequence();
		
		// "Keepalive" to the drone
		if(lastCommandSentAt+1000 >= System.currentTimeMillis()) {
			moveToTargetHeight();
			lastCommandSentAt = System.currentTimeMillis();
		}

	}

}
