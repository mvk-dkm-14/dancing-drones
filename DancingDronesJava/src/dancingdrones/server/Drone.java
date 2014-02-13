package dancingdrones.server;
/**
 * Görs i mån av tid, så att det ska bli enklare att kunna byta drones i framtiden
 * @author Rodoo
 */
public interface Drone extends Runnable {
	/**
	 * Sets a new height to the drone.
	 * @param m, float in meters
	 * @return true/false, if we were able to change
	 */
	public boolean setHeight(float m);
	
	/**
	 * Assign the drone to a group.
	 * @param group, the goup to assign it to.
	 * @return true/false
	 */
	public boolean setGroup(int group);
	
	/**
	 * Start the drone!
	 * @return true/false
	 */
	public boolean start();
	
	
	/**
	 * Stop the drone.
	 * @return
	 */
	public boolean stop();
	
	/**
	 * Move the drone to the following vector.
	 * @param forward, positive: forward, negative: backwards
	 * @param right, positive: right, negative: left
	 * @param up, positive: up, negative: down
	 * @return true/false;
	 */
	public boolean move(int forward, int right, int up);
	
	/**
	 * Get the drone movement vector
	 * @return an int[] with index 0:FW/BW, 1:R/L, 2:UP/DWN
	 */
	public int[] getMovement(); // Ändra till egen struct istället för int[]?
	
}