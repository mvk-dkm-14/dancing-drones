package dancingdrones.common;

public class Settings {
	public static final boolean DBG = true;
	
	public static final int 	LISTEN_PORT = 1337;
	public static final String 	IP_NET = "192.168.1.";
	public static final String 	IP_SERVER = "254";
	
	//public static final int 	MAX_CONNECTED_DRONES = 4;
	
	public static final int 	CONNECT_TIMEOUT 		= 3000;
	public static final int 	READ_UPDATE_DELAY_MS 	= 5;
	
	
	
	public static final void printDebug(String message) {
		System.out.println("[DEBUG] " + message); }
	public static final void printInfo(String message) {
		System.out.println("[INFO] " + message); }
	
}
