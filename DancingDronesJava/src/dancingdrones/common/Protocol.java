package dancingdrones.common;

import java.nio.ByteBuffer;


/**
 * Protocol overview
 * =================
 * Structure:
 * |  Main  | Selection |     Data    |
 * | 4bits  |   4bits	|  0-20 bytes |
 * |      1 byte        |  0-20 bytes | 
 * 
 * The first byte decides how big the rest of the packet is.
 * 
 * Example, move Drone 1 forward:
 * =====================================================================================
 * |  Type   |  Selection   | Drone ID (1 byte) | Command (1 byte) | Data (0-16 bytes) |
 * =====================================================================================
 * | Control | Single Drone |        1          |       Move       |  1f, 0f, 0f, 0f   |
 * =====================================================================================
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
	// Main functions:
	public static final byte INIT	 	= 	0x10; // 0x10, 0001 0000
	public static final byte LOGIN	 	=	0x20; // 0x20, 0010 0000
	public static final byte CONTROL 	=	0x30; // 0x30, 0011 0000
	public static final byte REQUEST 	=	0x40; // 0x40, 0100 0000
	public static final byte QUIT 	 	=	0x50; // 0x50, 0101 0000
	
	// Init selection:
	public static final byte I_FAILED	=	0x01; // 0x01, 0000 0001
	public static final byte I_OK 	 	=	0x02; // 0x02, 0000 0010


	// Drone selection:
	public static final byte C_SINGLE 	=	0x01; // 0x01, 0000 0001
	public static final byte C_GROUP  	=	0x02; // 0x02, 0000 0010
	public static final byte C_ALL 	  	=	0x03; // 0x03, 0000 0011
	
	// Drone functions:
	public static final byte TAKEOFF 	=	0x01;
	public static final byte LAND 		=	0x02;
	public static final byte MOVE		=	0x03;
	public static final byte EMERGENCY	=	0x04;
	public static final byte KILL 		=	0x05;
	
	
// ========================================================= //

	public static byte[] moveDrone(int id, float f, float x, float y, float z) {
		//	ByteBuffer.allocate(x).put(a).put(b).array() = byte[];
		return ByteBuffer
					.allocate(19)		// Allocate the array
					.put(0, (byte)(CONTROL + C_SINGLE))	// Header
					.put(1, (byte)id)	// Drone ID
					.put(2, MOVE)		// Command
					.putFloat(3, f)		// Data
					.putFloat(7, x)		// Data
					.putFloat(11, y)	// Data
					.putFloat(15, z)	// Data
					.array();			// return the array

	}
	
	
	
	
	
	
	public static byte getPacketType(byte[] b){
		return (byte)(b[0] & 0xF0);
	}
	
	public static byte[] setPacketType(byte[] b, byte type){
			b[0] += type;
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
