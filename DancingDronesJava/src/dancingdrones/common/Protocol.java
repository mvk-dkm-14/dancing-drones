package dancingdrones.common;

import java.nio.ByteBuffer;


/**
 * Protocol overview
 * =================
 * Structure:
 * | Type |Selection|Command|    Data    |
 * | 3bits|  2 bits | 3bits | 0-20 bytes |
 * |         1 byte         | 0-20 bytes | 
 * 
 * The first byte decides how big the rest of the packet is.
 * 
 * Example, move Drone 1 forward:
 * ============================================================================
 * |  Type   |  Selection   | Command | Drone ID (1 byte) | Data (0-16 bytes) |
 * ============================================================================
 * | Control | Single Drone |  Move   |        1          |  0f, 1f, 0f, 0f   |
 * ============================================================================
 *
 * Example, move start all drones:
 * =================================
 * |  Type   | Selection | Command |
 * =================================
 * | Control |     A     | Takeoff |
 * =================================
 * 
 * Main Functions:
 * I	: Initialize
 * CON	: Connect drone.
 * C	: Control drone(s)
 * R	: Request info (updates)
 * Q	: Quit
 *
 * Drone Selection:
 * S		: Single Drone
 * G		: Group of Drones
 * A		: All Drones
 * 
 * Drone function:
 * T		Takoff		: Starts the drone
 * L		Land		: Drone enter landing mode
 * M[X,Y,Z] Move 		: Move the drone by tilting it in the desired direction
 * E		Emergency	: Turn off the drone immediately (even mid air)
 * K		Kill, same as E 
 */
public class Protocol {

	/**
	 * Stores all mask values relevant to the protocol
	 * @author Rodoo
	 */
	public static class Mask {
		public static final byte TYPE 	= 	(byte)0xE0; // 0xE0, 1110 0000
		public static final byte SELECT	=	(byte)0x18; // 0x18, 0001 1000
		public static final byte COMMAND = 	(byte)0x07; // 0x07, 0000 0111
	}
	
	/**
	 * Stores the sizes of the different packets from the client
	 * @author Rodoo
	 */
	public static class Size {
		public static final byte INIT		=	1;
		public static final byte CONNECT	=	2;
		public static final byte ID			=	2;
		public static final byte C_MOVE		=	17;
		public static final byte C_TAKEOFF	=	2;
		public static final byte C_LAND		=	2;
		public static final byte C_EMERGENCY	=	2;
		public static final byte C_TESTFLIGHT	=	2;
		public static final byte QUIT		=	1;
	}
	
	/**
	 * Stores the indexes of different values.
	 * @author Rodoo
	 */
	public static class Index {
		public static final byte H_TYPE		=	0;
		public static final byte H_SELECT	=	1;
		public static final byte H_COMMAND	=	2;
		
		public static final byte DATA_OFFSET = 1;
		
	}
	
	/**
	 * Stores the values of parts of the protocol
	 * H_: Type
	 * S_: Select
	 * C_: Command
	 * @author Rodoo
	 */
	public static class Value {
		// Main types:
		//public static final byte M_TYPE 	= 	(byte)0xE0; // 0xE0, 1110 0000
		public static final byte T_INIT	 	= 	(byte)0x20; // 0x20, 0010 0000
		//public static final byte LOGIN	 =	(byte)0x40; // 0x40, 0100 0000
		public static final byte T_CONNECT	=	(byte)0x40; // 0x40, 0100 0000
		public static final byte T_CONTROL 	=	(byte)0x60; // 0x60, 0110 0000
		public static final byte T_REQUEST 	=	(byte)0x80; // 0x80, 1000 0000
		public static final byte T_QUIT 		=	(byte)0xA0; // 0xA0, 1010 0000
		
		// Init selection:	
		//public static final byte M_SELECT		=	0x18; // 0x18, 0001 1000
		
		public static final byte S_I_FAILED	=	0x00; // 0x00, 0000 0000
		public static final byte S_I_OK		=	0x08; // 0x08, 0000 1000
		public static final byte S_I_REQUEST 	=	0x10; // 0x10, 0001 0000	
		// Drone selection:							  // 0x18, 0001 1000
		public static final byte S_D_SINGLE 	=	0x08; // 0x08, 0000 1000
		public static final byte S_D_GROUP  	=	0x10; // 0x10, 0001 0000
		public static final byte S_D_ALL 	  	=	0x18; // 0x18, 0001 1000
		
		// Drone commands:							  
		//public static final byte M_COMMAND	= 	0x07; // 0x07, 0000 0111
		public static final byte C_TAKEOFF 	=	0x01; // 0x01, 0000 0001
		public static final byte C_LAND 		=	0x02; // 0x02, 0000 0010
		public static final byte C_MOVE		=	0x03; // 0x03, 0000 0101
		public static final byte C_EMERGENCY	=	0x04; // 0x04, 0000 0100
		public static final byte C_TESTFLIGHT	=	0x05; // 0x05, 0000 0101 
	}
	
	/**
	 * Extracts the information from the header for easier comparison.
	 * @param h the header to extract data from
	 * @return byte[], where index 0=type, 1=select and 2=command
	 */
	public static byte[] extractHeader(byte h){
		return ByteBuffer
				.allocate(3)
				.put(0, (byte)(h & Mask.TYPE))
				.put(1, (byte)(h & Mask.SELECT))
				.put(2, (byte)(h & Mask.COMMAND))
				.array();
	}
	
	/**
	 * Returns the size of the packet given the packet header, aka first byte.
	 * @param header
	 * @return total size of the packet, including header. -1 if it's an unknown header.
	 */
	public static int getPacketSize(byte header){
		// Extract fields from header
		byte[] b = extractHeader(header);
		
		int size = 0;
		// Get the default size for our different headers
		switch(b[Index.H_TYPE]){	// Switch packet type
		case Value.T_INIT:			// INIT
			size =  Size.INIT;		// Return INIT packet size
			break;
		case Value.T_CONNECT:		// Connect drone!
			size = Size.CONNECT;	
			break;
		case Value.T_CONTROL:		// Control drone(s)!
			switch(b[Index.H_COMMAND]){
			case(Value.C_TESTFLIGHT):	
				size = Size.C_TESTFLIGHT; 
				break;
			case(Value.C_MOVE):		
				size = Size.C_MOVE;		
				break;
			case(Value.C_LAND):		
				size = Size.C_LAND;		// All drones
				break;
			case(Value.C_TAKEOFF):	
				size = Size.C_TAKEOFF;		// All drones
				break;
			case(Value.C_EMERGENCY):
				size = Size.C_EMERGENCY;			// All drones
				break;
			default:
				Settings.printDebug("Unknown Control command: "+b[2]);
				return -1;
			}	
			break;
		case Value.T_QUIT:				// Quit
			return Size.QUIT;
		default:
			Settings.printDebug("Unknown header received");
			return -1;
		}
		// If it's not for all, the data field starts with id (one byte) 
		if(b[Index.H_COMMAND] == Value.S_D_ALL) size--;
		return size;
	}
	
	public static byte[] sendTestFlightDrone(int id) {
		return ByteBuffer
				.allocate(2)
				.put(0, (byte)(Value.T_CONTROL + Value.S_D_SINGLE + Value.C_TESTFLIGHT))
				.put(1, (byte)id)
				.array();
	}
	
// ========================================================= //
//							CLIENT							 //
// ========================================================= //
	
	public static byte[] sendInitRequest(){
		return ByteBuffer
				.allocate(1)
				.put(0, (byte)(Value.T_INIT + Value.S_I_REQUEST))
				.array();
	}
	
	public static byte[] sendConnectDrone(int id) {
		return ByteBuffer
				.allocate(2)
				.put(0, (byte)(Value.T_CONNECT + Value.S_D_SINGLE))
				.put(1, (byte)id)
				.array();
	}
	
	public static byte[] sendTakeOff(int id) {
		return ByteBuffer
					.allocate(2)
					.put(0, (byte)(Value.T_CONTROL + Value.S_D_SINGLE + Value.C_TAKEOFF))
					.put(1, (byte)id)
					.array();
	}
	
	public static byte[] sendLand(int id) {
		return ByteBuffer
					.allocate(2)
					.put(0, (byte)(Value.T_CONTROL + Value.S_D_SINGLE + Value.C_LAND))
					.put(1, (byte)id)
					.array();
	}
	
	public static byte[] sendEmergency(int id){
		return ByteBuffer
					.allocate(2)
					.put(0, (byte)(Value.T_CONTROL + Value.S_D_SINGLE + Value.C_EMERGENCY))
					.put(1, (byte)id)
					.array();
	}

	public static byte[] sendMoveSingleDrone(int id, float lr, float fb, float vs, float as) {
		//	ByteBuffer.allocate(x).put(a).put(b).array() = byte[];
		return ByteBuffer
					.allocate(18)		// Allocate the array
					.put(0, (byte)(Value.T_CONTROL + Value.S_D_SINGLE + Value.C_MOVE))	// Header
					.put(1, (byte)id)	// Drone ID
					.putFloat(3, lr)	// Data
					.putFloat(7, fb)	// Data
					.putFloat(11, vs)	// Data
					.putFloat(15, as)	// Data
					.array();			// return the array

	}
	
	
// ======================================================== //
//							SERVER							//
//========================================================= //
	
	public static byte[] initResponse(boolean r){
		if(r)
			return ByteBuffer
					.allocate(1)
					.put((byte)(Value.T_INIT + Value.S_I_OK))
					.array();
		else
			return ByteBuffer
					.allocate(1)
					.put((byte)(Value.T_INIT + Value.S_I_FAILED))
					.array();	
	}
	
	public static byte[] sendQuit(){
		return ByteBuffer
					.allocate(1)
					.put((byte)Value.T_QUIT)
					.array();
	}
	
	
	
	public static byte getPacketType(byte[] b){
		return (byte)(b[0] & 0xE0);
	}
	
	public static byte getSelection(byte[] b){
		return (byte)(b[0] & 0x18);
	}
	
	public static byte getDroneID(byte[] b){
		return b[1];
	}
	
	/**
	 * Takes a raw byte packet and extracts the movement floats
	 * @param b - the raw byte packet go fetch the floats from
	 * @param i - index of the first float  of the four to read
	 * @return	a float array with four floats [F,X,Y,Z]
	 */
	public static final float[] getMovement(byte[] b, int i){
		float[] r = {
					ByteBuffer.allocate(4).getFloat(i), 
					ByteBuffer.allocate(4).getFloat(i+4),
					ByteBuffer.allocate(4).getFloat(i+8),
					ByteBuffer.allocate(4).getFloat(i+12)};
		
		return r;
	}
}
