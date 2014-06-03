package dancingdrones.common;

/*
		| Main  |  Sub/Sel  |  ID   | func, data |
		| char  |	char    | short | char, N/A  |
 Size(b):	1		  1			2		1	4*4		= 21 bytes


[function][]
Ex: S1T			: Starta drönare 1 (Takeoff)
	AM[0.5,0,0]	: Alla drönare flyger frammåt med halv lutning [FW,R,U]

Main Functions:
L	: Login
C	: Control drone(s)
I	: Init
R	: Request info (updates)
Q	: Quit

Drone Selection:
S[1-N]	: Single Drone
G[1-N]	: Group of Drones
A		: All Drones

Drone function:
T		Takoff		: Starts the drone
L		Land		: Drone enter landing mode
M[X,Y,Z] Move 		: Move the drone by tilting it in the desired direction
E		Emergency	: Turn of the drone immediately (even midair)
K		Kill, same as E
*/

public class Protocol {
	private static final int CLIENT_PACKET_SIZE = 21;
	// Main functions:
	public static final char LOGIN = 'L';
	public static final char CONTROL = 'C';
	public static final char INIT = 'I';
	public static final char REQUEST = 'R';
	public static final char QUIT = 'Q';
	
	// Drone selection:
	public static final char SINGLE = 'S';
	public static final char GROUP = 'G';
	public static final char ALL = 'A';
	
	// Drone functions:
	public static final char TAKEOFF = 'T';
	public static final char LAND = 'L';
	public static final char MOVE = 'M';
	public static final char EMERGENCY = 'E';
	public static final char KILL = 'K';
	
	// Init functions:
	public static final String INIT_OK = "OK";
	
	
// ========================================================= //
	
	public static byte[] generateClientByteBuffer(){
		return new byte[CLIENT_PACKET_SIZE];
	}
	
	public static char getPacketType(byte[] b){
		return (char)b[0];
	}
	
	public static byte[] setPacketType(byte[] b, char type){
		b[0] = (byte)type;
		return b;
	}
	
	public static char getSelectionType(byte[] b){
		return (char)b[0];
	}
	
	public static byte[] setSelection(byte[] b, char selection){
		b[1] = (byte)selection;
		return b;
	}
	
	/**
	 * Takes a raw byte packet and extracts the movement floats
	 * @param b - the raw byte packet go fetch the floats from
	 * @return	a float array with four floats [F,X,Y,Z]
	 */
	public static final float[] getMovement(byte[] b){
		// Read the float values from raw bytes
		// Move float values are stored between byte 20-35
		float f1 = (b[20] & 0xFF) 
				| ((b[21] & 0xFF) << 8) 
				| ((b[22] & 0xFF) << 16) 
				| ((b[23] & 0xFF) << 24);
		float f2 = (b[24] & 0xFF) 
				| ((b[25] & 0xFF) << 8) 
				| ((b[26] & 0xFF) << 16) 
				| ((b[27] & 0xFF) << 24);
		float f3 = (b[28] & 0xFF) 
				| ((b[29] & 0xFF) << 8) 
				| ((b[30] & 0xFF) << 16) 
				| ((b[31] & 0xFF) << 24);
		float f4 = (b[32] & 0xFF) 
				| ((b[33] & 0xFF) << 8) 
				| ((b[34] & 0xFF) << 16) 
				| ((b[35] & 0xFF) << 24);
		// Collect the floats in an array and return it
		float[] r = {f1, f2, f3, f4};
		return r;
	}
}
