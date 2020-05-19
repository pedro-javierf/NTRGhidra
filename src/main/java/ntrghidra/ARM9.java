package ntrghidra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntrghidra.CRT0.AutoLoadEntry;

public class ARM9
{
	private int RamAddress;

	private byte[] StaticData;

	private int _start_ModuleParamsOffset;
	private CRT0.ModuleParams _start_ModuleParams;

	private List<CRT0.AutoLoadEntry> AutoLoadList;

	public ARM9(byte[] Data, int RamAddress)
	{
		this(Data, RamAddress, FindModuleParams(Data));
	}

	public ARM9(byte[] Data, int RamAddress, int _start_ModuleParamsOffset)
	{
		//Unimportant static footer! Use it for _start_ModuleParamsOffset and remove it.
		if (IOUtil.ReadU32LE(Data, Data.length - 12) == 0xDEC00621)
		{
			_start_ModuleParamsOffset = IOUtil.ReadU32LE(Data, Data.length - 8);
			byte[] data_tmp = new byte[Data.length - 12];
			//Array.Copy(Data, data_tmp, Data.length - 12);
			
			data_tmp = Arrays.copyOf(Data, Data.length - 12);
			
			Data = data_tmp;
		}

		this.RamAddress = RamAddress;
		this._start_ModuleParamsOffset = _start_ModuleParamsOffset;
		
		CRT0 a = new CRT0();
		_start_ModuleParams = a.new ModuleParams(Data, _start_ModuleParamsOffset);
		
		if (_start_ModuleParams.CompressedStaticEnd != 0)
		{
			Data = Decompress(Data, _start_ModuleParamsOffset);
			_start_ModuleParams = a.new ModuleParams(Data, _start_ModuleParamsOffset);
		}

		StaticData = new byte[_start_ModuleParams.AutoLoadStart - RamAddress];
		//Array.Copy(Data, StaticData, _start_ModuleParams.AutoLoadStart - RamAddress);
		StaticData = Arrays.copyOf(Data, _start_ModuleParams.AutoLoadStart - RamAddress);
		
		AutoLoadList = new ArrayList<CRT0.AutoLoadEntry>();
		int nr = (_start_ModuleParams.AutoLoadListEnd - _start_ModuleParams.AutoLoadListOffset) / 0xC;
		int Offset = _start_ModuleParams.AutoLoadStart - RamAddress;
		for (int i = 0; i < nr; i++)
		{
			AutoLoadEntry entry = a.new AutoLoadEntry(Data, _start_ModuleParams.AutoLoadListOffset - RamAddress + i * 0xC);
			entry.Data = new byte[entry.Size];
			
			
			//GOOD ?
			//Array.Copy(Data, Offset, entry.Data, 0, entry.Size);
			entry.Data = Arrays.copyOf(Data, entry.Size);
			
			
			
			AutoLoadList.add(entry);
			Offset += entry.Size;
		}
	}

	/*
	public byte[] Write()
	{
		MemoryStream m = new MemoryStream();
		EndianBinaryWriter er = new EndianBinaryWriter(m, Endianness.LittleEndian);
		er.Write(StaticData, 0, StaticData.length);
		_start_ModuleParams.AutoLoadStart = (int)er.BaseStream.Position + RamAddress;
		foreach (var v in AutoLoadList) er.Write(v.Data, 0, v.Data.length);
		_start_ModuleParams.AutoLoadListOffset = (int)er.BaseStream.Position + RamAddress;
		foreach (var v in AutoLoadList) v.Write(er);
		_start_ModuleParams.AutoLoadListEnd = (int)er.BaseStream.Position + RamAddress;
		long curpos = er.BaseStream.Position;
		er.BaseStream.Position = _start_ModuleParamsOffset;
		_start_ModuleParams.Write(er);
		er.BaseStream.Position = curpos;
		byte[] data = m.ToArray();
		er.Close();
		return data;
	}*/

	public void AddAutoLoadEntry(int Address, byte[] Data)
	{
		CRT0 a = new CRT0();
		AutoLoadList.add(a.new AutoLoadEntry(Address,Data));
		//AutoLoadList.add(new CRT0.AutoLoadEntry(Address, Data));
	}

	public boolean WriteU16LE(int Address, short Value)
	{
		if (Address > RamAddress && Address < _start_ModuleParams.AutoLoadStart)
		{
			IOUtil.WriteU16LE(StaticData, (int)(Address - RamAddress), Value);
			return true;
		}
		for(AutoLoadEntry v : AutoLoadList)
		{
			if (Address > v.Address && Address < (v.Address + v.Size))
			{
				IOUtil.WriteU16LE(v.Data, (int)(Address - v.Address), Value);
				return true;
			}
		}
		return false;
	}

	public int ReadU32LE(int Address)
	{
		if (Address > RamAddress && Address < _start_ModuleParams.AutoLoadStart)
		{
			return IOUtil.ReadU32LE(StaticData, (int)(Address - RamAddress));
		}
		for(AutoLoadEntry v : AutoLoadList)
		{
			if (Address > v.Address && Address < (v.Address + v.Size))
			{
				return IOUtil.ReadU32LE(v.Data, (int)(Address - v.Address));
			}
		}
		return 0xFFFFFFFF;
	}

	public boolean WriteU32LE(int Address, int Value)
	{
		if (Address > RamAddress && Address < _start_ModuleParams.AutoLoadStart)
		{
			IOUtil.WriteU32LE(StaticData, (int)(Address - RamAddress), Value);
			return true;
		}
		for(AutoLoadEntry v : AutoLoadList)
		{
			if (Address > v.Address && Address < (v.Address + v.Size))
			{
				IOUtil.WriteU32LE(v.Data, (int)(Address - v.Address), Value);
				return true;
			}
		}
		return false;
	}

	public static byte[] Decompress(byte[] Data)
	{
		int offset = FindModuleParams(Data);
		if (offset == 0xffffffe3) 
		{
			System.out.println("No moduleparams found: uncompressed!");
			return Data;//no moduleparams, so it must be uncompressed
		}
		else
		{
			System.out.println("moduleparams found: trying to decompress");
			return Decompress(Data, offset);
		}
	}

	public static byte[] Decompress(byte[] Data, int _start_ModuleParamsOffset)
	{
		if (IOUtil.ReadU32LE(Data, _start_ModuleParamsOffset + 0x14) == 0) 
			return Data;//Not Compressed!
		
		byte[] Result = CRT0.MIi_UncompressBackward(Data);
		IOUtil.WriteU32LE(Result, _start_ModuleParamsOffset + 0x14, 0); //sets 4 bytes to 0x0
		
		return Result;
	}

	private static int FindModuleParams(byte[] Data)
	{
		return (int)(IndexOf(Data, new byte[] { 0x21, 0x06, (byte) 0xC0, (byte) 0xDE, (byte) 0xDE, (byte) 0xC0, 0x06, 0x21 }) - 0x1C);
	}

	private static long IndexOf(byte[] Data, byte[] Search)
	{
		long index = -1;
		boolean found = false;
		for(int i = 0; i < Data.length; i++)
		{
			if(Data[i]==Search[0]) //byte is a primitive type
			{
				found = true;
				for(int x = i+1; x < i+Search.length && x < Data.length; x++)
				{
					if(Data[x]!=Search[x-i])
					{
						found = false;
						break;
					}
				}
			}
			if(found) {break;}
		}
		return index;
	}
	
	/*
	private static long IndexOf(byte[] Data, byte[] Search)
	{
		fixed (byte* H = Data) fixed (byte* N = Search)
		{
			long i = 0;
			for (byte* hNext = H, hEnd = H + Data.length; hNext < hEnd; i++, hNext++)
			{
				boolean Found = true;
				for (byte* hInc = hNext, nInc = N, nEnd = N + Search.length; Found && nInc < nEnd; Found = *nInc == *hInc, nInc++, hInc++) ;
				if (Found) return i;
			}
			return -1;
		}
	}*/
}
