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
	private int CONNECT_TIMEOUT = 3000;
	private int READ_UPDATE_DELAY_MS = 5;
	
	private long nextOut = 0;
	
	private AtomicBoolean ready = new AtomicBoolean(false);
	private AtomicBoolean flying = new AtomicBoolean(false);
	private AtomicInteger battery = new AtomicInteger(0);
	private AtomicBoolean running = new AtomicBoolean(false);
	
	private LinkedList<float[]> moveQueue = new LinkedList<float[]>();
	
	private int lastSequence=0;
	
	//private AtomicReference<ARDrone> drone;
	private ARDrone drone;
	private int id;
	private float targetHeight;
	private int group;
	

	
	public ARDroneController(int id) throws IOException, InterruptedException{
		Settings.printDebug("Creating drone with ip ."+ id);
		this.id = id;
		drone = new ARDrone(InetAddress.getByName(Settings.IP_NET + id));
		//drone = new AtomicReference<ARDrone>(new ARDrone(InetAddress.getByName(Settings.IP_NET + id)));
		
		// Tell the drone to send updates to this instance (object)
		drone.addStatusChangeListener(this);
		drone.addNavDataListener(this);
		startUpdateLoop();
		//initDrone();
	}
	
    private void startUpdateLoop() {
        Thread thread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                updateLoop();
            }
        });
        thread.setName("ARDrone Control Loop");
        thread.start();
    }
	
	private void updateLoop(){
		if(running.get())
			return;
		running.set(true);
		try {
			System.err.println("Connecting to the drone");
			drone.connect();
			drone.waitForReady(CONNECT_TIMEOUT);
			drone.clearEmergencySignal();
			System.err.println("Connected to the drone");
			try {
				while(running.get()){
					if(flying.get()) {
						if(moveQueue.size() > 0) {
							float[] m = moveQueue.getFirst();
				            if(m[0] != 0 || m[1] != 0 || m[2] != 0 || m[3] != 0)
				            	drone.move(m[0], m[1], m[2], m[3]);
						}
			            else {
			                drone.hover();
			            }
			        } else {
			        	// Not flying
			        }
			        try {
			            Thread.sleep(READ_UPDATE_DELAY_MS);
			        } catch(InterruptedException e) {
			            // Ignore
			        }
			    }
			}
			finally {
			    drone.disconnect();
			}
		} catch (IOException e) {
			
		}
	}
	
	public void initDrone() throws IOException, InterruptedException{
		// Create ARDrone object,
		// connect to drone and initialize it.
		drone.connect();
		Thread.sleep(1000);
		drone.clearEmergencySignal();
		
		// Tell the drone we want navigation data from it.
		Thread.sleep(100);
		drone.sendDemoNavigationData();
		
		// Wait until drone is ready
		//drone.waitForReady(CONNECT_TIMEOUT);
		Settings.printDebug("Waiting for drone to be ready..");
		while(!ready.get())
				Thread.sleep(100);
		Settings.printDebug("Drone is now Ready!");
				
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
		drone.connect();
		drone.clearEmergencySignal();
		Thread.sleep(1000);
		
		// Wait until drone is ready
		drone.waitForReady(CONNECT_TIMEOUT);
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
		this.ready.set(true);
	}

	@Override
	public void navDataReceived(NavData nd) {
		if(nd.getSequence() < lastSequence) {
			// Old data received
			Settings.printDebug("ARDroneController: Received old navdata");
			return;
		}
		// Fresh data received
		this.flying.set(nd.isFlying());
		
		if(System.currentTimeMillis()>nextOut) {
			Settings.printDebug("Altitude: "+nd.getAltitude()+"m");
			Settings.printDebug("Battery level: "+nd.getBattery()+"%");
			nextOut = System.currentTimeMillis()+1000;
		}
		Settings.printDebug("Navdata!");
		
		lastSequence = nd.getSequence();

	}

}
