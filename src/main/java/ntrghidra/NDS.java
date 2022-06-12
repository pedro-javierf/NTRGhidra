/*
* NDS.java
* Definition of the NDS binary format
*
* Pedro Javier Fernández
* 12/06/2022 (DD/MM/YYYY)
*
* See project license file for license information.
*/ 

package ntrghidra;

import java.io.IOException;

import ghidra.app.util.bin.BinaryReader;
import ghidra.app.util.bin.ByteProvider;

public class NDS
{
	public RomHeader Header;
	public NitroFooter StaticFooter;
	public byte[] SubRom;
	public byte[][] FileData;
	public byte[] RSASignature;
	public byte[] MainRom;
	public RomOVT[] MainOvt;
	public RomOVT[] SubOvt;
	
	//Auxiliary Classes
	//NOTE: SubRom refers to the ARM7 binary
	
	public class RomHeader
	{
		public String GameName;//12
		public String GameCode;//4
		public String MakerCode;//2
		public Byte ProductId;
		public Byte DeviceType;
		public Byte DeviceSize;
		public byte ReservedA;//9
		public Byte GameVersion;
		public Byte Property;

		//Arm9
		public int MainRomOffset;
		public int MainEntryAddress;
		public int MainRamAddress;
		public int MainSize;
		
		//Arm7
		public int SubRomOffset;
		public int SubEntryAddress;
		public int SubRamAddress;
		public int SubSize;
		
		//???
		public int FntOffset;
		public int FntSize;
		public int FatOffset;
		public int FatSize;
		
		//Overlay Table
		public int MainOvtOffset;
		public int MainOvtSize; //OverlayTable Size
		public int SubOvtOffset;
		public int SubOvtSize;
		
		//Others
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
	}

	public class NitroFooter
	{
		public int NitroCode;
		public int _start_ModuleParamsOffset;
		public int Unknown;
		
		public NitroFooter() { }
		public NitroFooter(BinaryReader er) throws IOException
		{
			NitroCode = er.readNextInt();
			_start_ModuleParamsOffset = er.readNextInt();
			Unknown = er.readNextInt();
		}
		
	}

	public static class RomOVT
			{
				public int Id;
				public int RamAddress;
				public int RamSize;
				public int BssSize;
				public int SinitInit;
				public int SinitInitEnd;
				public int FileId;
				
				//The next elements are all within 4 bytes. Most significan 8 bits are flags. 24 are compressed offset in the overlay file
				public int CompressedOffset;//:24; offset
				public OVTFlag Flag;		// :8;
				
				public class OVTFlag
				{
					private boolean Compressed;
					private boolean AuthenticationCode;
					
					OVTFlag(byte msb) {
						
						Compressed = (0x000000FF & (int)msb & 0x1)==1;
						AuthenticationCode = (0x000000FF & (int)msb & 0x2)==1;
					}
					public boolean getCompressed() {return Compressed;}
					public boolean getAuthenticationCode() {return AuthenticationCode;}
				}
				
				public RomOVT() { }
				public RomOVT(BinaryReader er) throws IOException
				{
					
					Id = er.readNextInt(); //0-4
					RamAddress = er.readNextInt(); //4-8
					RamSize = er.readNextInt(); //8-C
					BssSize = er.readNextInt(); //C-10
					SinitInit = er.readNextInt(); //static start 10-14
					SinitInitEnd = er.readNextInt(); //static end 14-18
					FileId = er.readNextInt(); //18-1C
					
					int tmp = er.readNextInt(); //1C
					CompressedOffset = tmp & 0x00FFFFFF;
					Flag = new OVTFlag((byte)(tmp >>> 24));
				}

				
			}

	//=================
	
	public NDS() { }
	public NDS(ByteProvider provider) throws IOException
	{
		//Little endian reader
		BinaryReader er = new BinaryReader(provider, true); 
		
		Header = new RomHeader(er);
		er.setPointerIndex(Header.MainRomOffset);

		MainRom = er.readNextByteArray(Header.MainSize);
		if (er.readNextInt() == 0xDEC00621)
		{
			er.setPointerIndex(er.getPointerIndex()-4);
			StaticFooter = new NitroFooter(er);
		}

		er.setPointerIndex(Header.SubRomOffset);
		SubRom = er.readNextByteArray(Header.SubSize);

		//er.setPointerIndex(Header.FntOffset);
		//er.BaseStream.Position = Header.FntOffset;
		//Fnt = new RomFNT(er);

		
		
		//	Populate the overlay tables
		er.setPointerIndex(Header.MainOvtOffset);
		MainOvt = new RomOVT[Header.MainOvtSize / 32];
		for (int i = 0; i < Header.MainOvtSize / 32; i++) 
			MainOvt[i] = new RomOVT(er);

		er.setPointerIndex(Header.SubOvtOffset);
		SubOvt = new RomOVT[Header.SubOvtSize / 32];
		for (int i = 0; i < Header.SubOvtSize / 32; i++) 
			SubOvt[i] = new RomOVT(er);

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
	}

	public int getHeaderMainSize()
	{
		return Header.MainSize;
	}
	
	public RomOVT[] getMainOVT()
	{
		return MainOvt;
	}
	
	public RomOVT[] getSubOVT()
	{
		return SubOvt;
	}
	
	public byte[] GetDecompressedARM9()
	{
		if (StaticFooter != null) 
			return ARM9.Decompress(MainRom, StaticFooter._start_ModuleParamsOffset);
		
		return ARM9.Decompress(MainRom);
	}
	
	public byte[] GetDecompressedOverlay(byte[] input)
	{
		return ARM9.DecompressOVT(input);
	}

}
