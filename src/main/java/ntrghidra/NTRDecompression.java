package ntrghidra;

import java.io.DataOutputStream;
import java.io.IOException;

//Very specific class to handle the Nintendo SDK compression. Original code by Gericom ported to java by pedro-javierf
public class NTRDecompression {

	private static byte[] ModuleParamsMAGIC = { (byte) 0x21, (byte) 0x06, (byte)0xC0, (byte) 0xDE, (byte) 0xDE, (byte) 0xC0, (byte) 0x06, (byte) 0x21 };
	
	public static void writeIntLE(int value,DataOutputStream out) throws IOException {
		  out.writeByte(value & 0xFF);
		  out.writeByte((value >> 8) & 0xFF);
		  out.writeByte((value >> 16) & 0xFF);
		  out.writeByte((value >> 24) & 0xFF);
		}
	
	private static long IndexOf(byte[] Data, byte[] Search)
	{
		boolean found = false;
		int index = -1;
		for(int i = 0; i < Data.length; i++)
		{
			if(Data[i] == Search[0]) //good :l ??
			{
				if(Data[i+1]==Search[i+1] &&
						Data[i+2]==Search[i+2] &&
								Data[i+3]==Search[i+3] &&
										Data[i+4]==Search[i+4] &&
												Data[i+5]==Search[i+5] &&
														Data[i+6]==Search[i+6] &&
																Data[i+7]==Search[i+7])
				{
					index = i;
				}
								
			}
		}
		return index;
	}
	
	private static int FindModuleParams(byte[] Data)
	{
		return ((int)IndexOf(Data, ModuleParamsMAGIC) - 0x1C);
	}
	
	public static byte[] Decompress(byte[] Data)
	{
		int offset = FindModuleParams(Data);
		if (offset == 0xffffffe3) return Data;//no moduleparams, so it must be uncompressed
		return Decompress(Data, offset);
	}
	
	public static byte[] Decompress(byte[] Data, int _start_ModuleParamsOffset)
	{
		if (IOUtil.ReadU32LE(Data, (int)_start_ModuleParamsOffset + 0x14) == 0) 
			return Data;//Not Compressed!
		
		byte[] Result = MIi_UncompressBackward(Data);
		writeIntLE(Result, (int)_start_ModuleParamsOffset + 0x14, 0);
		return Result;
	}
	
	public static byte[] MIi_UncompressBackward(byte[] Data)
	{
		int leng = IOUtil.ReadU32LE(Data, Data.length - 4) + (int)Data.length;
		byte[] Result = new byte[leng];
		Array.Copy(Data, Result, Data.length);
		int Offs = (int)(Data.length - (IOUtil.ReadU32LE(Data, Data.length - 8) >> 24));
		int dstoffs = (int)leng;
		while (true)
		{
			byte header = Result[--Offs];
			for (int i = 0; i < 8; i++)
			{
				if ((header & 0x80) == 0) Result[--dstoffs] = Result[--Offs];
				else
				{
					byte a = Result[--Offs];
					byte b = Result[--Offs];
					int offs = (((a & 0xF) << 8) | b) + 2;//+ 1;
					int length = (a >> 4) + 2;
					do
					{
						Result[dstoffs - 1] = Result[dstoffs + offs];
						dstoffs--;
						length--;
					}
					while (length >= 0);
				}
				if (Offs <= (Data.length - (IOUtil.ReadU32LE(Data, Data.length - 8) & 0xFFFFFF))) return Result;
				header <<= 1;
			}
		}
	}
	
	
}
