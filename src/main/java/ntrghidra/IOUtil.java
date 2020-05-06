package ntrghidra;

public class IOUtil {

	
	//Is this ok?
	public static int ReadU32LE(byte[] Data, int Offset)
	{
		return (Data[Offset] | (Data[Offset + 1] << 8) | (Data[Offset + 2] << 16) | (Data[Offset + 3] << 24));
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
