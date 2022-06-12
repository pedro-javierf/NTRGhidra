/*
* NDSMemRegionList.java
* Definition of NDSMemRegionList class and
* list of these memory regions.
*
* Pedro Javier Fernández
* 12/06/2022 (DD/MM/YYYY)
*
* See project license file for license information.
*/ 

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
		_ARM9regions.add(new NDSMemRegion("Shared_WRAM",0x03000000,0x00008000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("ARM9_IO_Ports",0x04000000,0x01000000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("Standard_Palettes",0x05000000,0x01000000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM_Engine_A_BG_VRAM",0x06000000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM_Engine_B_BG_VRAM",0x06200000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM_Engine_A_OBJ_VRAM",0x06400000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM_Engine_B_OBJ_VRAM",0x06600000,0x00200000,true,false,false));
		_ARM9regions.add(new NDSMemRegion("VRAM_LCDC",0x06800000,0x00200000,true,false,false));
		
		//ARM7
		_ARM7regions.add(new NDSMemRegion("Shared_WRAM",0x03000000,0x00008000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("ARM7_WRAM_",0x03800000,0x00010000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("ARM7_IO_Ports",0x04000000,0x00800000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("Wireless_Communications_Wait_State_0(8KB_RAM_at_4804000h)",0x04800000,0x00008000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("Wireless_Communications_Wait_State_1(IO_Ports_at_4808000h)",0x04808000,0x00200000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("VRAM_allocated_as_Work_RAM_to_ARM7(max_256K)",0x06000000,0x0040000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("GBA_Slot_ROM(max_32MB)",0x06600000,0x02000000,true,false,false));
		_ARM7regions.add(new NDSMemRegion("GBA_Slot_RAM(max_64KB)",0x0A000000,0x00010000,true,false,false));
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
