package ntrghidra;

import java.io.DataOutputStream;
import java.io.IOException;

import org.python.bouncycastle.util.Arrays;

public class CRT0
{
	public class ModuleParams
	{
		public ModuleParams(byte[] Data, int Offset)
		{
			AutoLoadListOffset = IOUtil.ReadU32LE(Data, (int)Offset + 0);
			AutoLoadListEnd = IOUtil.ReadU32LE(Data, (int)Offset + 4);
			AutoLoadStart = IOUtil.ReadU32LE(Data, (int)Offset + 8);
			StaticBssStart = IOUtil.ReadU32LE(Data, (int)Offset + 12);
			StaticBssEnd = IOUtil.ReadU32LE(Data, (int)Offset + 16);
			CompressedStaticEnd = IOUtil.ReadU32LE(Data, (int)Offset + 20);
			SDKVersion = IOUtil.ReadU32LE(Data, (int)Offset + 24);
			NitroCodeBE = IOUtil.ReadU32LE(Data, (int)Offset + 28);
			NitroCodeLE = IOUtil.ReadU32LE(Data, (int)Offset + 32);
		}
		
		public void Write(/*EndianBinaryWriter*/DataOutputStream er) throws IOException
		{
			er.writeInt(AutoLoadListOffset);
			er.writeInt(AutoLoadListEnd);
			er.writeInt(AutoLoadStart);
			er.writeInt(StaticBssStart);
			er.writeInt(StaticBssEnd);
			er.writeInt(CompressedStaticEnd);
			er.writeInt(SDKVersion);
			er.writeInt(NitroCodeBE);
			er.writeInt(NitroCodeLE);
		}
		public int AutoLoadListOffset;
		public int AutoLoadListEnd;
		public int AutoLoadStart;
		public int StaticBssStart;
		public int StaticBssEnd;
		public int CompressedStaticEnd;
		public int SDKVersion;
		public int NitroCodeBE;
		public int NitroCodeLE;
	}

	public class AutoLoadEntry
	{
		public AutoLoadEntry(int Address, byte[] Data)
		{
			this.Address = Address;
			this.Data = Data;
			Size = Data.length;
			BssSize = 0;
		}
		public AutoLoadEntry(byte[] Data, int Offset) //segments (?)
		{
			Address = IOUtil.ReadU32LE(Data, Offset + 0);
			Size = IOUtil.ReadU32LE(Data, Offset + 4);
			BssSize = IOUtil.ReadU32LE(Data, Offset + 8);
		}
		public void Write(DataOutputStream er) throws IOException
		{
			er.writeInt(Address);
			er.writeInt(Size);
			er.writeInt(BssSize);
		}
		public int Address;
		public int Size;
		public int BssSize;

		public byte[] Data;
	}

	public static byte[] MIi_UncompressBackward(byte[] Data)
	{
		//Reads the end of the Data[], exactly the last 4 bytes, which are the length
		int leng = IOUtil.ReadU32LE(Data, Data.length - 4) + Data.length;
		byte[] Result = new byte[leng];
		
		
		//Array.Copy(Data, Result, Data.length);
		Result = Arrays.copyOf(Data, Data.length);
		
		int Offs = (int)(Data.length - (IOUtil.ReadU32LE(Data, Data.length - 8) >> 24));
		System.out.println("Offs: " + Offs);
		int dstoffs = leng;
		while (true)
		{
			
			byte header = Result[--Offs];
			for (int i = 0; i < 8; i++)
			{
				if ((header & 0x80) == 0)
				{
					Result[--dstoffs] = Result[--Offs];
				}
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
				
				if (Offs <= (Data.length - (IOUtil.ReadU32LE(Data, Data.length - 8) & 0xFFFFFF))) 
					return Result;
				
				header <<= 1;
			}
		}
	}
}
