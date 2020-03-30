package ntrghidra;

import java.io.ObjectOutputStream;
import java.util.List;

public class FileAllocationEntry
{
	public FileAllocationEntry(int Offset, int Size)
	{
		fileTop = Offset;
		fileBottom = Offset + Size;
	}
	public FileAllocationEntry(EndianBinaryReader er)
	{
		fileTop = er.Readint();
		fileBottom = er.Readint();
	}
	public void Write(ObjectOutputStream er)
	{
		er.writeInt(fileTop);
		er.writeInt(fileBottom);
	}
	public int fileTop;
	public int fileBottom;

	public int fileSize()
	{
		return (fileBottom - fileTop);
	}
	
	public class DirectoryTableEntry
	{
		public DirectoryTableEntry() { }
		public DirectoryTableEntry(EndianBinaryReader er)
		{
			dirEntryStart = er.Readint();
			dirEntryFileID = er.Readshort();
			dirParentID = er.Readshort();
		}
		public void Write(ObjectOutputStream er)
		{
			er.writeInt(dirEntryStart);
			er.writeInt(dirEntryFileID);
			er.writeInt(dirParentID);
		}
		public int dirEntryStart;
		public short dirEntryFileID;
		public short dirParentID;
	}
	public class EntryNameTableEntry
	{
		protected EntryNameTableEntry() { }
		public EntryNameTableEntry(EndianBinaryReader er)
		{
			entryNamelength = er.ReadByte();
		}
		public void Write(ObjectOutputStream er)
		{
			er.writeInt(entryNamelength);
		}
		public Byte entryNamelength;
	}
	public class EntryNameTableEndOfDirectoryEntry// : EntryNameTableEntry
	{
		public EntryNameTableEndOfDirectoryEntry() { }
		public EntryNameTableEndOfDirectoryEntry(EndianBinaryReader er)
		{base(er);}
		
		public void Write(ObjectOutputStream er)
		{
			base.writeInt(er);
		}
	}
	public class EntryNameTableFileEntry// : EntryNameTableEntry
	{
		public EntryNameTableFileEntry(String Name)
		{
			entryNamelength = (byte)Name.length;
			entryName = Name;
		}
		public EntryNameTableFileEntry(EndianBinaryReader er)
			: base(er)
		{
			entryName = er.ReadString(Encoding.ASCII, entryNamelength);
		}
		@Override
		public void Write(EndianBinaryWriter er)
		{
			base.Write(er);
			er.Write(entryName, Encoding.ASCII, false);
		}
		public String entryName;
	}
	public class EntryNameTableDirectoryEntry// : EntryNameTableEntry
	{
		public EntryNameTableDirectoryEntry(String Name, short DirectoryID)
		{
			entryNamelength = (byte)(Name.length() | 0x80);
			entryName = Name;
			directoryID = DirectoryID;
		}
		public EntryNameTableDirectoryEntry(EndianBinaryReader er)
			: base(er)
		{
			entryName = er.ReadString(Encoding.ASCII, entryNamelength & 0x7F);
			directoryID = er.Readshort();
		}
		@Override
		public void Write(EndianBinaryWriter er)
		{
			base.Write(er);
			er.Write(entryName, Encoding.ASCII, false);
			er.Write(directoryID);
		}
		public String entryName;
		public short directoryID;
	}

	public class NitroFSUtil
	{
		public static void GenerateDirectoryTable(List<DirectoryTableEntry> directoryTable, SFSDirectory dir)
		{
			DirectoryTableEntry cur = new DirectoryTableEntry();
			if (dir.IsRoot)
			{
				cur.dirParentID = (short)(dir.TotalNrSubDirectories + 1);
				cur.dirEntryStart = cur.dirParentID * 8u;
			}
			else cur.dirParentID = dir.Parent.DirectoryID;
			dir.DirectoryID = (short)(0xF000 + directoryTable.Count);
			directoryTable.Add(cur);
			foreach (SFSDirectory d in dir.SubDirectories)
			{
				GenerateDirectoryTable(directoryTable, d);
			}
		}

		public static void GenerateEntryNameTable(List<DirectoryTableEntry> directoryTable, List<EntryNameTableEntry> entryNameTable, SFSDirectory dir, ref int Offset, ref short FileId)
		{
			directoryTable[dir.DirectoryID - 0xF000].dirEntryStart = Offset;
			directoryTable[dir.DirectoryID - 0xF000].dirEntryFileID = FileId;

			for(SFSDirectory d : dir.SubDirectories)
			{
				entryNameTable.Add(new EntryNameTableDirectoryEntry(d.DirectoryName, d.DirectoryID));
				Offset += (int)d.DirectoryName.length + 3u;
			}
			for(SFSFile f : dir.Files)
			{
				f.FileID = FileId;
				entryNameTable.Add(new EntryNameTableFileEntry(f.FileName));
				Offset += (int)f.FileName.length + 1u;
				FileId++;
			}
			entryNameTable.Add(new EntryNameTableEndOfDirectoryEntry());
			Offset++;

			for(SFSDirectory d : dir.SubDirectories)
			{
				GenerateEntryNameTable(directoryTable, entryNameTable, d, ref Offset, ref FileId);
			}
		}
	}
	
}

