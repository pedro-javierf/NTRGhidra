package ntrghidra;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteArrayProvider;

public class NDS
{
	public NDS() { }
	public NDS(byte[] data) throws IOException
	{
		//EndianBinaryReader er = new EndianBinaryReader(new MemoryStream(data), Endianness.LittleEndian);
		
		ByteArrayProvider provider = new ByteArrayProvider(data);  
		BinaryReader er = new BinaryReader(provider, true); //little endian :)
		
		
		
		Header = new RomHeader(er);

		er.setPointerIndex(Header.MainRomOffset);
		
		//er.BaseStream.Position = Header.MainRomOffset; //sets the stream pointer (aka position)
		

		
		MainRom = er.readNextByteArray(Header.MainSize);
		if (er.readNextInt() == 0xDEC00621)//Nitro Footer!
		{
			//er.BaseStream.Position -= 4;
			er.setPointerIndex(er.getPointerIndex()-4);
			StaticFooter = new NitroFooter(er);
		}

		//er.BaseStream.Position = Header.SubRomOffset;
		er.setPointerIndex(Header.SubRomOffset);
		SubRom = er.readNextByteArray(Header.SubSize);

		er.setPointerIndex(Header.FntOffset);
		//er.BaseStream.Position = Header.FntOffset;
		//Fnt = new RomFNT(er);

		er.setPointerIndex(Header.MainOvtOffset);
		//er.BaseStream.Position = Header.MainOvtOffset;
		//MainOvt = new RomOVT[Header.MainOvtSize / 32];
		//for (int i = 0; i < Header.MainOvtSize / 32; i++) MainOvt[i] = new RomOVT(er);

		er.setPointerIndex(Header.SubOvtOffset);
		//er.BaseStream.Position = Header.SubOvtOffset;
		//SubOvt = new RomOVT[Header.SubOvtSize / 32];
		//for (int i = 0; i < Header.SubOvtSize / 32; i++) SubOvt[i] = new RomOVT(er);

		er.setPointerIndex(Header.FatOffset);
		//er.BaseStream.Position = Header.FatOffset;
		
		/*
		Fat = new FileAllocationEntry[Header.FatSize / 8];
		for (int i = 0; i < Header.FatSize / 8; i++) Fat[i] = new FileAllocationEntry(er);

		if (Header.BannerOffset != 0)
		{
			er.setPointerIndex(Header.BannerOffset);
			//er.BaseStream.Position = Header.BannerOffset;
			//Banner = new RomBanner(er);
		}*/

		/*
		FileData = new byte[Header.FatSize / 8][];
		for (int i = 0; i < Header.FatSize / 8; i++)
		{
			//er.setPointerIndex(Fat[i].fileTop);
			//er.BaseStream.Position = Fat[i].fileTop;
			//FileData[i] = er.ReadBytes((int)Fat[i].fileSize);
		}*/
		/*
		//RSA Signature
		if (Header.RomSize + 0x88 <= er.BaseStream.length)
		{
			er.setPointerIndex(Header.RomSize);
			//er.BaseStream.Position = Header.RomSize;
			byte[] RSASig = er.readNextByteArray(0x88);
			for (int i = 0; i < RSASig.length; i++)
			{
				//It could be padding, so check if there is something other than 0xFF or 0x00
				if (RSASig[i] != 0xFF || RSASig[i] != 0x00)
				{
					RSASignature = RSASig;
					break;
				}
			}
		}
		*/
		//er.Close();
	}

	public RomHeader Header;
	
	public class RomHeader
	{
		public RomHeader() { }
		public RomHeader(BinaryReader er) throws IOException
		{
			int index = 0;
			
			GameName = er.readAsciiString(index, 12); index+=12;
			GameCode = er.readAsciiString(index, 4); index+=4;
			MakerCode = er.readAsciiString(index, 2); index+=2;
			ProductId = er.readByte(index); index++;
			DeviceType = er.readByte(index); index++;
			DeviceSize = er.readByte(index); index++;
			ReservedA = er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index);er.readByte(index); index+=9;
			GameVersion = er.readByte(index); index++;
			Property = er.readByte(index); index++;

			MainRomOffset = er.readInt(index); index+=4;
			MainEntryAddress = er.readInt(index); index+=4;
			MainRamAddress = er.readInt(index); index+=4;
			MainSize = er.readInt(index); index+=4;
			SubRomOffset = er.readInt(index); index+=4;
			SubEntryAddress = er.readInt(index); index+=4;
			SubRamAddress = er.readInt(index); index+=4;
			SubSize = er.readInt(index); index+=4;

			FntOffset = er.readInt(index); index+=4;
			FntSize = er.readInt(index); index+=4;

			FatOffset = er.readInt(index); index+=4;
			FatSize = er.readInt(index); index+=4;

			MainOvtOffset = er.readInt(index); index+=4;
			MainOvtSize = er.readInt(index); index+=4;

			SubOvtOffset = er.readInt(index); index+=4;
			SubOvtSize = er.readInt(index); index+=4;
			
			

			RomParamA = er.readByteArray(index, 8); index+=8;
			BannerOffset = er.readInt(index); index+=4;
			SecureCRC = er.readShort(index); index+=2;
			RomParamB = er.readByteArray(index,2); index+=2;

			MainAutoloadDone = er.readInt(index); index+=4;
			SubAutoloadDone = er.readInt(index); index+=4;

			RomParamC = er.readByteArray(index,8); index+=8;
			RomSize = er.readInt(index); index+=4;
			HeaderSize = er.readInt(index); index+=4;
			ReservedB = er.readByteArray(index,0x38); index+=0x38;

			LogoData = er.readByteArray(index,0x9C); index+=0x9C;
			LogoCRC = er.readShort(index); index+=2;
			HeaderCRC = er.readShort(index); index+=2;
		}
		
		public String GameName;//12
		public String GameCode;//4
		public String MakerCode;//2
		public Byte ProductId;
		public Byte DeviceType;
		public Byte DeviceSize;
		public byte ReservedA;//9
		public Byte GameVersion;
		public Byte Property;

		
		public int MainRomOffset;
		public int MainEntryAddress;
		public int MainRamAddress;
		
		public int MainSize;
		
		public int SubRomOffset;
		public int SubEntryAddress;
		public int SubRamAddress;
		
		public int SubSize;

		
		public int FntOffset;
		
		public int FntSize;

		
		public int FatOffset;
		
		public int FatSize;

		
		public int MainOvtOffset;
		
		public int MainOvtSize;

		
		public int SubOvtOffset;
		
		public int SubOvtSize;

		public byte[] RomParamA;//8
		
		public int BannerOffset;
		public short SecureCRC;
		public byte[] RomParamB;//2

		public int MainAutoloadDone;
		public int SubAutoloadDone;

		public byte[] RomParamC;//8

		public int RomSize;

		public int HeaderSize;
		public byte[] ReservedB;//0x38

		public byte[] LogoData;//0x9C

		public short LogoCRC;

		public short HeaderCRC;
	}
	
	public byte[] MainRom;
	public NitroFooter StaticFooter;

	public class NitroFooter
	{
		public NitroFooter() { }
		public NitroFooter(BinaryReader er) throws IOException
		{
			NitroCode = er.readNextInt();
			_start_ModuleParamsOffset = er.readNextInt();
			Unknown = er.readNextInt();
		}
		public int NitroCode;
		public int _start_ModuleParamsOffset;
		public int Unknown;
	}
	
	public byte[] SubRom;

	public byte[][] FileData;

	public byte[] RSASignature;
	



	public byte[] GetDecompressedARM9()
	{
		//StaticFooter = new NitroFooter(er);
		if (StaticFooter != null) return ARM9.Decompress(MainRom, StaticFooter._start_ModuleParamsOffset);
		else return ARM9.Decompress(MainRom);
	}

}
