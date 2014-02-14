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
 * L	: Login
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

	public static class var {
		// Packet sizes:
		public static final byte INIT_SIZE	=	1;		
		
		// Main types:
		public static final byte TYPE_MASK 	= 	(byte)0xE0; // 0xE0, 1110 0000
		public static final byte INIT	 	= 	(byte)0x20; // 0x20, 0010 0000
		public static final byte LOGIN	 	=	(byte)0x40; // 0x40, 0100 0000
		public static final byte CONTROL 	=	(byte)0x60; // 0x60, 0110 0000
		public static final byte REQUEST 	=	(byte)0x80; // 0x80, 1000 0000
		public static final byte QUIT 	 	=	(byte)0xA0; // 0xA0, 1010 0000
		
		// Init selection:	
		public static final byte SELECT_MASK =	0x18; // 0x18, 0001 1000
		
		public static final byte I_FAILED	=	0x00; // 0x00, 0000 0000
		public static final byte I_OK		=	0x08; // 0x08, 0000 1000
		public static final byte I_REQUEST 	=	0x10; // 0x10, 0001 0000	
		// Drone selection:							  // 0x18, 0001 1000
		public static final byte C_SINGLE 	=	0x01; // 0x08, 0000 1000
		public static final byte C_GROUP  	=	0x02; // 0x10, 0001 0000
		public static final byte C_ALL 	  	=	0x03; // 0x18, 0001 1000
		
		// Drone commands:							  
		public static final byte COMMAND_MASK = 0x07; // 0x07, 0000 0111
		public static final byte TAKEOFF 	=	0x01; // 0x01, 0000 0001
		public static final byte LAND 		=	0x02; // 0x02, 0000 0010
		public static final byte MOVE		=	0x03; // 0x03, 0000 0101
		public static final byte EMERGENCY	=	0x04; // 0x04, 0000 0100
		public static final byte KILL 		=	0x05; // 0x05, 0000 0101
		
		// Drone/Group ID:
		public static final byte DATA_OFFSET = 2; 
		
	}	
	
// ========================================================= //
//							CLIENT							 //
// ========================================================= //
	
	public static byte[] initRequest(){
		return ByteBuffer
				.allocate(1)
				.put(0, (byte)(var.INIT + var.I_REQUEST))
				.array();
	}
	
	public static byte[] takeoff(int id) {
		return ByteBuffer
					.allocate(3)
					.put(0, (byte)(var.CONTROL + var.C_SINGLE))
					.put(1, (byte)id)
					.put(2, var.TAKEOFF)
					.array();
	}
	
	public static byte[] land(int id) {
		return ByteBuffer
					.allocate(3)
					.put(0, (byte)(var.CONTROL + var.C_SINGLE))
					.put(1, (byte)id)
					.put(2, var.LAND)
					.array();
	}

	public static byte[] moveDrone(int id, float f, float x, float y, float z) {
		//	ByteBuffer.allocate(x).put(a).put(b).array() = byte[];
		return ByteBuffer
					.allocate(19)		// Allocate the array
					.put(0, (byte)(var.CONTROL + var.C_SINGLE))	// Header
					.put(1, (byte)id)	// Drone ID
					.put(2, var.MOVE)	// Command
					.putFloat(3, f)		// Data
					.putFloat(7, x)		// Data
					.putFloat(11, y)	// Data
					.putFloat(15, z)	// Data
					.array();			// return the array

	}
	
	
// ======================================================== //
//							SERVER
//========================================================= //
	
	public static byte[] initResponse(boolean r){
		if(r)
			return ByteBuffer
					.allocate(1)
					.put((byte)(var.INIT + var.I_OK))
					.array();
		else
			return ByteBuffer
					.allocate(1)
					.put((byte)(var.INIT + var.I_FAILED))
					.array();	
	}
	
	public static byte[] sendQuit(){
		return ByteBuffer
					.allocate(1)
					.put((byte)var.QUIT)
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
		// Read the float values from raw bytes
//		float f1 = (b[i+0] & 0xFF) 
//				| ((b[i+1] & 0xFF) << 8) 
//				| ((b[i+2] & 0xFF) << 16) 
//				| ((b[i+3] & 0xFF) << 24);
//		float f2 = (b[i+4] & 0xFF) 
//				| ((b[i+5] & 0xFF) << 8) 
//				| ((b[i+6] & 0xFF) << 16) 
//				| ((b[i+7] & 0xFF) << 24);
//		float f3 = (b[i+8] & 0xFF) 
//				| ((b[i+9] & 0xFF) << 8) 
//				| ((b[i+10] & 0xFF) << 16) 
//				| ((b[i+11] & 0xFF) << 24);
//		float f4 = (b[i+12] & 0xFF) 
//				| ((b[i+13] & 0xFF) << 8) 
//				| ((b[i+14] & 0xFF) << 16) 
//				| ((b[i+15] & 0xFF) << 24);
//		// Collect the floats in an array and return it
//		float[] r = {f1, f2, f3, f4};
//		return r;
	}
}
