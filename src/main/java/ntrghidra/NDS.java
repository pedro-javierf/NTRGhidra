package ntrghidra;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class NDS
{
	public NDS() { }
	public NDS(byte[] data)
	{
		EndianBinaryReader er = new EndianBinaryReader(new MemoryStream(data), Endianness.LittleEndian);
		Header = new RomHeader(er);

		ObjectInputStream er = new ObjectInputStream();
		
		er.BaseStream.Position = Header.MainRomOffset;
		MainRom = er.ReadBytes((int)Header.MainSize);
		if (er.Readint() == 0xDEC00621)//Nitro Footer!
		{
			er.BaseStream.Position -= 4;
			StaticFooter = new NitroFooter(er);
		}

		er.BaseStream.Position = Header.SubRomOffset;
		SubRom = er.ReadBytes((int)Header.SubSize);

		er.BaseStream.Position = Header.FntOffset;
		Fnt = new RomFNT(er);

		er.BaseStream.Position = Header.MainOvtOffset;
		MainOvt = new RomOVT[Header.MainOvtSize / 32];
		for (int i = 0; i < Header.MainOvtSize / 32; i++) MainOvt[i] = new RomOVT(er);

		er.BaseStream.Position = Header.SubOvtOffset;
		SubOvt = new RomOVT[Header.SubOvtSize / 32];
		for (int i = 0; i < Header.SubOvtSize / 32; i++) SubOvt[i] = new RomOVT(er);

		er.BaseStream.Position = Header.FatOffset;
		Fat = new FileAllocationEntry[Header.FatSize / 8];
		for (int i = 0; i < Header.FatSize / 8; i++) Fat[i] = new FileAllocationEntry(er);

		if (Header.BannerOffset != 0)
		{
			er.BaseStream.Position = Header.BannerOffset;
			Banner = new RomBanner(er);
		}

		FileData = new byte[Header.FatSize / 8][];
		for (int i = 0; i < Header.FatSize / 8; i++)
		{
			er.BaseStream.Position = Fat[i].fileTop;
			FileData[i] = er.ReadBytes((int)Fat[i].fileSize);
		}
		//RSA Signature
		if (Header.RomSize + 0x88 <= er.BaseStream.length)
		{
			er.BaseStream.Position = Header.RomSize;
			byte[] RSASig = er.ReadBytes(0x88);
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
		er.Close();
	}

	public byte[] Write()
	{
		MemoryStream m = new MemoryStream();
		EndianBinaryWriter er = new EndianBinaryWriter(m, Endianness.LittleEndian);
		//Header
		//skip the header, and write it afterwards
		er.BaseStream.Position = 16384;
		Header.HeaderSize = (int)er.BaseStream.Position;
		//MainRom
		Header.MainRomOffset = (int)er.BaseStream.Position;
		Header.MainSize = (int)MainRom.length;
		er.Write(MainRom, 0, MainRom.length);
		//Static Footer
		if (StaticFooter != null) StaticFooter.Write(er);
		if (MainOvt.length != 0)
		{
			while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0);
			//Main Ovt
			Header.MainOvtOffset = (int)er.BaseStream.Position;
			Header.MainOvtSize = (int)MainOvt.length * 0x20;
			for(var v : MainOvt) v.Write(er);
			for(var v : MainOvt)
			{
				while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0);
				Fat[v.FileId].fileTop = (int)er.BaseStream.Position;
				Fat[v.FileId].fileBottom = (int)er.BaseStream.Position + (int)FileData[v.FileId].length;
				er.Write(FileData[v.FileId], 0, FileData[v.FileId].length);
			}
		}
		else
		{
			Header.MainOvtOffset = 0;
			Header.MainOvtSize = 0;
		}
		while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0xFF);
		//SubRom
		Header.SubRomOffset = (int)er.BaseStream.Position;
		Header.SubSize = (int)SubRom.length;
		er.Write(SubRom, 0, SubRom.length);
		//I assume this works the same as the main ovt?
		if (SubOvt.length != 0)
		{
			while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0);
			//Sub Ovt
			Header.SubOvtOffset = (int)er.BaseStream.Position;
			Header.SubOvtSize = (int)SubOvt.length * 0x20;
			for(var v : SubOvt) v.Write(er);
			for(var v : SubOvt)
			{
				while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0);
				Fat[v.FileId].fileTop = (int)er.BaseStream.Position;
				Fat[v.FileId].fileBottom = (int)er.BaseStream.Position + (int)FileData[v.FileId].length;
				er.Write(FileData[v.FileId], 0, FileData[v.FileId].length);
			}

		}
		else
		{
			Header.SubOvtOffset = 0;
			Header.SubOvtSize = 0;
		}
		while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0xFF);
		//FNT
		Header.FntOffset = (int)er.BaseStream.Position;
		Fnt.Write(er);
		Header.FntSize = (int)er.BaseStream.Position - Header.FntOffset;
		while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0xFF);
		//FAT
		Header.FatOffset = (int)er.BaseStream.Position;
		Header.FatSize = (int)Fat.length * 8;
		//Skip the fat, and write it after writing the data itself
		er.BaseStream.Position += Header.FatSize;
		//Banner
		if (Banner != null)
		{
			while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0xFF);
			Header.BannerOffset = (int)er.BaseStream.Position;
			Banner.Write(er);
		}
		else Header.BannerOffset = 0;
		//Files
		for (int i = (int)(Header.MainOvtSize / 32 + Header.SubOvtSize / 32); i < FileData.length; i++)
		{
			while ((er.BaseStream.Position % 0x200) != 0) er.Write((byte)0xFF);
			Fat[i].fileTop = (int)er.BaseStream.Position;
			Fat[i].fileBottom = (int)er.BaseStream.Position + (int)FileData[i].length;
			er.Write(FileData[i], 0, FileData[i].length);
		}
		while ((er.BaseStream.Position % 4/*0x200*/) != 0) er.Write((byte)0);
		long curpos = er.BaseStream.Position;
		Header.RomSize = (int)er.BaseStream.Position;
		int CapacitySize = Header.RomSize;
		CapacitySize |= CapacitySize >> 16;
		CapacitySize |= CapacitySize >> 8;
		CapacitySize |= CapacitySize >> 4;
		CapacitySize |= CapacitySize >> 2;
		CapacitySize |= CapacitySize >> 1;
		CapacitySize++;
		if (CapacitySize <= 0x20000) CapacitySize = 0x20000;
		int Capacity = -18;
		while (CapacitySize != 0) { CapacitySize >>= 1; Capacity++; }
		Header.DeviceSize = (byte)((Capacity < 0) ? 0 : Capacity);
		//RSA!
		if (RSASignature != null) er.Write(RSASignature, 0, 0x88);
		//Fat
		er.BaseStream.Position = Header.FatOffset;
		foreach (var v in Fat) v.Write(er);
		//Header
		er.BaseStream.Position = 0;
		Header.Write(er);
		byte[] result = m.ToArray();
		er.Close();
		return result;
	}

	public RomHeader Header;
	
	public class RomHeader
	{
		public RomHeader() { }
		public RomHeader(EndianBinaryReader er)
		{
			GameName = er.ReadString(Encoding.ASCII, 12).Replace("\0", "");
			GameCode = er.ReadString(Encoding.ASCII, 4).Replace("\0", "");
			MakerCode = er.ReadString(Encoding.ASCII, 2).Replace("\0", "");
			ProductId = er.ReadByte();
			DeviceType = er.ReadByte();
			DeviceSize = er.ReadByte();
			ReservedA = er.ReadBytes(9);
			GameVersion = er.ReadByte();
			Property = er.ReadByte();

			MainRomOffset = er.Readint();
			MainEntryAddress = er.Readint();
			MainRamAddress = er.Readint();
			MainSize = er.Readint();
			SubRomOffset = er.Readint();
			SubEntryAddress = er.Readint();
			SubRamAddress = er.Readint();
			SubSize = er.Readint();

			FntOffset = er.Readint();
			FntSize = er.Readint();

			FatOffset = er.Readint();
			FatSize = er.Readint();

			MainOvtOffset = er.Readint();
			MainOvtSize = er.Readint();

			SubOvtOffset = er.Readint();
			SubOvtSize = er.Readint();

			RomParamA = er.ReadBytes(8);
			BannerOffset = er.Readint();
			SecureCRC = er.Readshort();
			RomParamB = er.ReadBytes(2);

			MainAutoloadDone = er.Readint();
			SubAutoloadDone = er.Readint();

			RomParamC = er.ReadBytes(8);
			RomSize = er.Readint();
			HeaderSize = er.Readint();
			ReservedB = er.ReadBytes(0x38);

			LogoData = er.ReadBytes(0x9C);
			LogoCRC = er.Readshort();
			HeaderCRC = er.Readshort();
		}
		public void Write(EndianBinaryWriter er)
		{
			MemoryStream m = new MemoryStream();
			EndianBinaryWriter ew = new EndianBinaryWriter(m, Endianness.LittleEndian);
			ew.Write(GameName.PadRight(12, '\0'), Encoding.ASCII, false);
			ew.Write(GameCode.PadRight(4, '\0'), Encoding.ASCII, false);
			ew.Write(MakerCode.PadRight(2, '\0'), Encoding.ASCII, false);
			ew.Write(ProductId);
			ew.Write(DeviceType);
			ew.Write(DeviceSize);
			ew.Write(ReservedA, 0, 9);
			ew.Write(GameVersion);
			ew.Write(Property);

			ew.Write(MainRomOffset);
			ew.Write(MainEntryAddress);
			ew.Write(MainRamAddress);
			ew.Write(MainSize);
			ew.Write(SubRomOffset);
			ew.Write(SubEntryAddress);
			ew.Write(SubRamAddress);
			ew.Write(SubSize);

			ew.Write(FntOffset);
			ew.Write(FntSize);

			ew.Write(FatOffset);
			ew.Write(FatSize);

			ew.Write(MainOvtOffset);
			ew.Write(MainOvtSize);

			ew.Write(SubOvtOffset);
			ew.Write(SubOvtSize);

			ew.Write(RomParamA, 0, 8);
			ew.Write(BannerOffset);
			ew.Write(SecureCRC);
			ew.Write(RomParamB, 0, 2);

			ew.Write(MainAutoloadDone);
			ew.Write(SubAutoloadDone);

			ew.Write(RomParamC, 0, 8);
			ew.Write(RomSize);
			ew.Write(HeaderSize);
			ew.Write(ReservedB, 0, 0x38);

			ew.Write(LogoData, 0, 0x9C);
			LogoCRC = CRC16.GetCRC16(LogoData);
			ew.Write(LogoCRC);

			byte[] header = m.ToArray();
			ew.Close();

			HeaderCRC = CRC16.GetCRC16(header);

			er.Write(header, 0, header.length);
			er.Write(HeaderCRC);
		}
		public String GameName;//12
		public String GameCode;//4
		public String MakerCode;//2
		public Byte ProductId;
		public Byte DeviceType;
		public Byte DeviceSize;
		public byte[] ReservedA;//9
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
	public Byte[] MainRom;
	public NitroFooter StaticFooter;

	public class NitroFooter
	{
		public NitroFooter() { }
		public NitroFooter(EndianBinaryReader er)
		{
			NitroCode = er.Readint();
			_start_ModuleParamsOffset = er.Readint();
			Unknown = er.Readint();
		}
		public void Write(EndianBinaryWriter er)
		{
			er.Write(NitroCode);
			er.Write(_start_ModuleParamsOffset);
			er.Write(Unknown);
		}
		public int NitroCode;
		public int _start_ModuleParamsOffset;
		public int Unknown;
	}


	public Byte[] SubRom;
	public RomFNT Fnt;
	public class RomFNT
	{
		public RomFNT() 
		{
			DirectoryTable = new List<DirectoryTableEntry>();
			EntryNameTable = new List<EntryNameTableEntry>();
		}
		public RomFNT(EndianBinaryReader er)
		{
			DirectoryTable = new List<DirectoryTableEntry>();
			DirectoryTable.Add(new DirectoryTableEntry(er));
			for (int i = 0; i < DirectoryTable[0].dirParentID - 1; i++)
			{
				DirectoryTable.Add(new DirectoryTableEntry(er));
			}
			EntryNameTable = new List<EntryNameTableEntry>();
			int dirend = 0;
			while (dirend < DirectoryTable[0].dirParentID)
			{
				byte entryNamelength = er.ReadByte();
				er.BaseStream.Position--;
				if (entryNamelength == 0)
				{
					EntryNameTable.Add(new EntryNameTableEndOfDirectoryEntry(er));
					dirend++;
				}
				else if (entryNamelength < 0x80) EntryNameTable.Add(new EntryNameTableFileEntry(er));
				else EntryNameTable.Add(new EntryNameTableDirectoryEntry(er));
			}
		}
		public void Write(EndianBinaryWriter er)
		{
			foreach (DirectoryTableEntry e in DirectoryTable) e.Write(er);
			foreach (EntryNameTableEntry e in EntryNameTable) e.Write(er);
		}
		public List<DirectoryTableEntry> DirectoryTable;
		public List<EntryNameTableEntry> EntryNameTable;
	}
	public RomOVT[] MainOvt;
	public RomOVT[] SubOvt;
	public class RomOVT
	{
		public enum OVTFlag : byte
		{
			Compressed = 1,
			AuthenticationCode = 2
		}
		public RomOVT() { }
		public RomOVT(EndianBinaryReader er)
		{
			Id = er.Readint();
			RamAddress = er.Readint();
			RamSize = er.Readint();
			BssSize = er.Readint();
			SinitInit = er.Readint();
			SinitInitEnd = er.Readint();
			FileId = er.Readint();
			int tmp = er.Readint();
			Compressed = tmp & 0xFFFFFF;
			Flag = (OVTFlag)(tmp >> 24);
		}
		public void Write(EndianBinaryWriter er)
		{
			er.Write(Id);
			er.Write(RamAddress);
			er.Write(RamSize);
			er.Write(BssSize);
			er.Write(SinitInit);
			er.Write(SinitInitEnd);
			er.Write(FileId);
			er.Write((int)((((int)Flag) & 0xFF) << 24 | (Compressed & 0xFFFFFF)));
		}
		public int Id;
		public int RamAddress;
		public int RamSize;
		public int BssSize;
		public int SinitInit;
		public int SinitInitEnd;
		public int FileId;

		public int Compressed;//:24;
		public OVTFlag Flag;// :8;
	}
	public FileAllocationEntry[] Fat;
	public RomBanner Banner;
	public class RomBanner
	{
		public RomBanner() { }
		public RomBanner(EndianBinaryReader er)
		{
			Header = new BannerHeader(er);
			Banner = new BannerV1(er);
		}
		public void Write(EndianBinaryWriter er)
		{
			Header.CRC16_v1 = Banner.GetCRC();
			Header.Write(er);
			Banner.Write(er);
		}
		public BannerHeader Header;
		public class BannerHeader
		{
			public BannerHeader() { }
			public BannerHeader(EndianBinaryReader er)
			{
				Version = er.ReadByte();
				ReservedA = er.ReadByte();
				CRC16_v1 = er.Readshort();
				ReservedB = er.ReadBytes(28);
			}
			public void Write(EndianBinaryWriter er)
			{
				er.Write(Version);
				er.Write(ReservedA);
				er.Write(CRC16_v1);
				er.Write(ReservedB, 0, 28);
			}
			public Byte Version;
			public Byte ReservedA;
			public short CRC16_v1;
			public Byte[] ReservedB;//28
		}
		public BannerV1 Banner;
		public class BannerV1
		{
			public BannerV1() { }
			public BannerV1(EndianBinaryReader er)
			{
				Image = er.ReadBytes(32 * 32 / 2);
				Pltt = er.ReadBytes(16 * 2);
				GameName = new string[6];
				GameName[0] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
				GameName[1] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
				GameName[2] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
				GameName[3] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
				GameName[4] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
				GameName[5] = er.ReadString(Encoding.Unicode, 128).Replace("\0", "");
			}
			public void Write(EndianBinaryWriter er)
			{
				er.Write(Image, 0, 32 * 32 / 2);
				er.Write(Pltt, 0, 16 * 2);
				foreach (string s in GameName) er.Write(GameName[0].PadRight(128, '\0'), Encoding.Unicode, false);
			}
			public Byte[] Image;//32*32/2
			public Byte[] Pltt;//16*2

			public String[] GameName;//6, 128 chars (UTF16-LE)

			public String[] Base64GameName
			{
				get
				{
					String[] b = new String[6];
					for (int i = 0; i < 6; i++)
					{
						b[i] = Convert.ToBase64String(Encoding.Unicode.GetBytes(GameName[i]));
					}
					return b;
				}
				set
				{
					GameName = new string[6];
					for (int i = 0; i < 6; i++)
					{
						GameName[i] = Encoding.Unicode.GetString(Convert.FromBase64String(value[i]));
					}
				}
			}

			public short GetCRC()
			{
				byte[] Data = new byte[2080];
				Array.Copy(Image, Data, 512);
				Array.Copy(Pltt, 0, Data, 512, 32);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[0].PadRight(128, '\0')), 0, Data, 544, 256);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[1].PadRight(128, '\0')), 0, Data, 544 + 256, 256);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[2].PadRight(128, '\0')), 0, Data, 544 + 256 * 2, 256);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[3].PadRight(128, '\0')), 0, Data, 544 + 256 * 3, 256);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[4].PadRight(128, '\0')), 0, Data, 544 + 256 * 4, 256);
				Array.Copy(Encoding.Unicode.GetBytes(GameName[5].PadRight(128, '\0')), 0, Data, 544 + 256 * 5, 256);
				return CRC16.GetCRC16(Data);
			}

			/*
			public Bitmap GetIcon()
			{
				return GPU.Textures.ToBitmap(Image, Pltt, 0, 32, 32, GPU.Textures.ImageFormat.PLTT16, GPU.Textures.CharFormat.CHAR, true);
			}*/
		}
	}
	public Byte[][] FileData;

	public Byte[] RSASignature;

	/*
	public void FromFileSystem(SFSDirectory Root)
	{
		int did = 0;
		int fid = MainOvt.length + SubOvt.length;
		Root.UpdateIDs(ref did, ref fid);
		//FATB.numFiles = (ushort)Root.TotalNrSubFiles;
		//List<byte> Data = new List<byte>();
		int nrfiles = Root.TotalNrSubFiles;
		FileAllocationEntry[] overlays = new FileAllocationEntry[MainOvt.length + SubOvt.length];
		Array.Copy(Fat, overlays, MainOvt.length + SubOvt.length);
		Fat = new FileAllocationEntry[(MainOvt.length + SubOvt.length) + nrfiles];
		Array.Copy(overlays, Fat, MainOvt.length + SubOvt.length);
		byte[][] overlaydata = new byte[MainOvt.length + SubOvt.length][];
		Array.Copy(FileData, overlaydata, MainOvt.length + SubOvt.length);
		FileData = new byte[(MainOvt.length + SubOvt.length) + nrfiles][];
		Array.Copy(overlaydata, FileData, MainOvt.length + SubOvt.length);
		//FATB.allocationTable.Clear();
		for (ushort i = (ushort)(MainOvt.length + SubOvt.length); i < nrfiles + MainOvt.length + SubOvt.length; i++)
		{
			var f = Root.GetFileByID(i);
			Fat[i] = new FileAllocationEntry(0, 0);
			FileData[i] = f.Data;
		}
		Fnt.DirectoryTable.Clear();
		NitroFSUtil.GenerateDirectoryTable(Fnt.DirectoryTable, Root);
		int offset2 = Fnt.DirectoryTable[0].dirEntryStart;
		ushort fileId = (ushort)(MainOvt.length + SubOvt.length);//0;
		Fnt.EntryNameTable.Clear();
		NitroFSUtil.GenerateEntryNameTable(Fnt.DirectoryTable, Fnt.EntryNameTable, Root, ref offset2, ref fileId);
	}*/

	public SFSDirectory ToFileSystem()
	{
		bool treereconstruct = false;//Some programs do not write the Directory Table well, so sometimes I need to reconstruct the tree based on the fnt, which is bad!
		List<SFSDirectory> dirs = new List<SFSDirectory>();
		dirs.Add(new SFSDirectory("/", true));
		dirs[0].DirectoryID = 0xF000;

		int nrdirs = Fnt.DirectoryTable[0].dirParentID;
		for (int i = 1; i < nrdirs; i++)
		{
			dirs.Add(new SFSDirectory((ushort)(0xF000 + i)));
		}
		for (int i = 1; i < nrdirs; i++)
		{
			if (Fnt.DirectoryTable[i].dirParentID - 0xF000 == i)
			{
				treereconstruct = true;
				for(SFSDirectory v : dirs)
				{
					v.Parent = null;
				}
				break;
			}
			dirs[i].Parent = dirs[Fnt.DirectoryTable[i].dirParentID - 0xF000];
		}
		if (!treereconstruct)
		{
			for (int i = 0; i < nrdirs; i++)
			{
				for (int j = 0; j < nrdirs; j++)
				{
					if (dirs[i] == dirs[j].Parent)
					{
						dirs[i].SubDirectories.Add(dirs[j]);
					}
				}
			}
		}
		int offset = nrdirs * 8;
		short fileid = Fnt.DirectoryTable[0].dirEntryFileID;
		SFSDirectory curdir = null;
		foreach (EntryNameTableEntry e in Fnt.EntryNameTable)
		{
			for (int i = 0; i < nrdirs; i++)
			{
				if (offset == Fnt.DirectoryTable[i].dirEntryStart)
				{
					curdir = dirs[i];
					break;
				}
			}
			if (e is EntryNameTableEndOfDirectoryEntry)
			{
				curdir = null;
				offset++;
			}
			else if (e is EntryNameTableFileEntry)
			{
				curdir.Files.Add(new SFSFile(fileid++, ((EntryNameTableFileEntry)e).entryName, curdir));
				offset += 1u + e.entryNamelength;
			}
			else if (e is EntryNameTableDirectoryEntry)
			{
				if (treereconstruct)
				{
					dirs[((EntryNameTableDirectoryEntry)e).directoryID - 0xF000].Parent = curdir;
					curdir.SubDirectories.Add(dirs[((EntryNameTableDirectoryEntry)e).directoryID - 0xF000]);
				}
				dirs[((EntryNameTableDirectoryEntry)e).directoryID - 0xF000].DirectoryName = ((EntryNameTableDirectoryEntry)e).entryName;
				offset += 3u + (e.entryNamelength & 0x7Fu);
			}
		}
		for (int i = (MainOvt.length + SubOvt.length); i < Fat.length; i++)
		{
			//byte[] data = new byte[fat[i].fileSize];
			//Array.Copy(FileData FIMG.fileImage, fat[i].fileTop, data, 0, data.length);
			dirs[0].GetFileByID((ushort)i).Data = FileData[i];//data;
		}
		return dirs[0];
	}

	public byte[] GetDecompressedARM9()
	{
		//StaticFooter = new NitroFooter(er);
		if (StaticFooter != null) return ARM9.Decompress(MainRom, StaticFooter._start_ModuleParamsOffset);
		else return ARM9.Decompress(MainRom);
	}

	public class NDSIdentifier : FileFormatIdentifier
	{
		public override string GetCategory()
		{
			return Category_Roms;
		}

		public override string GetFileDescription()
		{
			return "Nintendo DS Rom (NDS)";
		}

		public override string GetFileFilter()
		{
			return "Nintendo DS Rom (*.nds, *.srl)|*.nds;*.srl";
		}

		public override Bitmap GetIcon()
		{
			return null;
		}

		public override FormatMatch IsFormat(EFEFile File)
		{
			if (File.Name.ToLower().EndsWith(".nds") || File.Name.ToLower().EndsWith(".srl")) return FormatMatch.Extension;
			return FormatMatch.No;
		}

	}
}
