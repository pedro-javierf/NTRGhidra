/*
* IOUtil.java
* Helper methods to manipulate bytes
*
* Pedro Javier Fernández
* 12/06/2022 (DD/MM/YYYY)
*
* See project license file for license information.
*/ 

package ntrghidra;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IOUtil {

	
	//Is this ok?
	public static Integer ReadU32LE(byte[] Data, int Offset)
	{
		ByteBuffer bb = ByteBuffer.wrap(Data);
		bb.order( ByteOrder.LITTLE_ENDIAN);
		return bb.getInt(Offset);
		//return ((byte)Data[Offset] | (byte)(Data[Offset + 1] << 8) | (byte)(Data[Offset + 2] << 16) | (byte)(Data[Offset + 3] << 24));
	}
	
	public static int ReadU32BE(byte[] Data, int Offset)
	{
		return (Data[Offset] | (Data[Offset + 1]) | (Data[Offset + 2]) | (Data[Offset + 3]));
	}
	
	public static void WriteU32LE(byte[] Data, int Offset, int Value)
	{
		Data[Offset] = (byte)(Value & 0xFF);
		Data[Offset + 1] = (byte)((Value >> 8) & 0xFF);
		Data[Offset + 2] = (byte)((Value >> 16) & 0xFF);
		Data[Offset + 3] = (byte)((Value >> 24) & 0xFF);
	}
	
	public static short ReadU16BE(byte[] Data, int Offset)
	{
		return (short)((Data[Offset] << 8) | Data[Offset + 1]);
	}

	public static void WriteU16LE(byte[] Data, int Offset, short Value)
	{
		Data[Offset] = (byte)(Value & 0xFF);
		Data[Offset + 1] = (byte)((Value >> 8) & 0xFF);
	}
	
}
