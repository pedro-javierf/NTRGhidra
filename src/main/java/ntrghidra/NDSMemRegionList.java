package ntrghidra;

import java.util.ArrayList;
import java.util.List;

public class NDSMemRegionList {

	//This internal class represents a memory region.
	public class NDSMemRegion {
	    String name;
	    int addr;
	    long size;
	    boolean read;
	    boolean write;
	    boolean execute;
	    public NDSMemRegion(String name, int addr, int size, boolean read, boolean write, boolean execute) {
	            this.name = name;
	            this.addr = addr;
	            this.size = size;
	            this.read = read;
	            this.write = write;
	            this.execute = execute;
	    }
	    public String name() {return name;}
	    public int addr() {return addr;}
	    public long size() {return size;}
	    public boolean read() {return read;}
	    public boolean write() {return write;}
	    public boolean execute() {return execute;}
	}

	
	//This class uses a singleton pattern
	private static NDSMemRegionList _instance = null;
	private static List<NDSMemRegion> _ARM9regions = new ArrayList<NDSMemRegion>();
	private static List<NDSMemRegion> _ARM7regions = new ArrayList<NDSMemRegion>();
	
	private NDSMemRegionList()
	{	
		//ARM9
		//Main Memory is found on NTRGhidraLoader.java
		_ARM9regions.add(new NDSMemRegion("Shared WRAM",0x03000000,0x00008000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("ARM9 I/O Ports",0x04000000,0x01000000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("Standard Palettes",0x05000000,0x01000000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM - Engine A BG VRAM",0x06000000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM - Engine B BG VRAM",0x06200000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM - Engine A OBJ VRAM",0x06400000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM - Engine B OBJ VRAM",0x06600000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM - LCDC",0x06800000,0x00200000,true,false,false));
		
		//ARM7
		_ARM7regions.add(new NDSMemRegion("Shared WRAM",0x03000000,0x00008000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("ARM7 WRAM (Private memory?)",0x03800000,0x00010000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("ARM7 I/O Ports",0x04000000,0x00800000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("Wireless Communications Wait State 0 (8KB RAM at 4804000h)",0x04800000,0x00008000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("Wireless Communications Wait State 1 (I/O Ports at 4808000h)",0x04808000,0x00200000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("VRAM allocated as Work RAM to ARM7 (max 256K)",0x06000000,0x0040000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("GBA Slot ROM (max 32MB)",0x06600000,0x02000000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("GBA Slot RAM (max 64KB)",0x0A000000,0x00010000,true,false,false));
	}
	
	public static NDSMemRegionList getInstance()
	{
		if(_instance==null)
			_instance=new NDSMemRegionList();
		
		return _instance;
	}
	
	public List<NDSMemRegion> getARM9Regions()
	{
		return _ARM9regions;
	}
	
	public List<NDSMemRegion> getARM7Regions()
	{
		return _ARM7regions;
	}
}
