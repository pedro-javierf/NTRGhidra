/*
* DSILabelList.java
* Definition of DSILabel class which represents registers
* and other important addresses in memory exclusive to the
* DSi console. Also includes a list of these memory regions. 
*
* Pedro Javier Fernández
* 12/06/2022 (DD/MM/YYYY)
*
* See project license file for license information.
*/ 

package ntrghidra;

import java.util.ArrayList;
import java.util.List;


public class DSILabelList {
	//This internal class represents a location of interest (registers, i/o, etc)
	public class DSILabel {
	    String name;
	    int addr;

	    public DSILabel(String name, int addr) {
	            this.name = name;
	            this.addr = addr;
	    }
	    public String name() {return name;}
	    public int addr() {return addr;}

	}

	//This class uses a singleton pattern
	private static DSILabelList _instance = null;
	private static List<DSILabel> _ARM9labels = new ArrayList<DSILabel>();
	private static List<DSILabel> _ARM7labels = new ArrayList<DSILabel>();

	
	//https://problemkaputt.de/gbatek.htm#dsiomaps
	private DSILabelList()
	{	
		/*ARM 9*/
		//ARM9 Display Engine A
		_ARM9labels.add(new DSILabel("REG_DISPCNT", 0x4000000));
		_ARM9labels.add(new DSILabel("REG_DISPSTAT", 0x4000004));
		_ARM9labels.add(new DSILabel("REG_VCOUNT", 0x4000006));
		_ARM9labels.add(new DSILabel("2D_ENGINE_A", 0x4000008));
		_ARM9labels.add(new DSILabel("REG_DISP3DCNT", 0x4000060));
		_ARM9labels.add(new DSILabel("REG_DISPCAPCNT", 0x4000064));
		_ARM9labels.add(new DSILabel("REG_DISP_MMEM_FIFO", 0x4000068));
		_ARM9labels.add(new DSILabel("REG_MASTER_BRIGHT", 0x400006C));
		
		//ARM9 DMA, Timers, and Keypad
		_ARM9labels.add(new DSILabel("DMA_CHANNEL_0_to_3", 0x40000B0));
		_ARM9labels.add(new DSILabel("DMA_FILL", 0x40000E0));
		_ARM9labels.add(new DSILabel("TIMERS", 0x4000100));
		_ARM9labels.add(new DSILabel("REG_KEYINPUT", 0x4000130));
		_ARM9labels.add(new DSILabel("REG_KEYCNT", 0x4000132));
		
		//ARM9 IPC/ROM
		_ARM9labels.add(new DSILabel("REG_IPCSYNC", 0x4000180));
		_ARM9labels.add(new DSILabel("REG_IPCFIFOCNT", 0x4000184));
		_ARM9labels.add(new DSILabel("REG_IPCFIFOSEND", 0x4000188));
		_ARM9labels.add(new DSILabel("REG_AUXSPICNT", 0x40001A0));
		_ARM9labels.add(new DSILabel("REG_AUXSPIDATA", 0x40001A2));
		_ARM9labels.add(new DSILabel("Gamecard_bus_timing_control", 0x40001A4));
		_ARM9labels.add(new DSILabel("Gamecard_bus_8_byte_command_out", 0x40001A8));
		_ARM9labels.add(new DSILabel("Gamecard_Encryption_Seed_0_Lower_32bit", 0x40001B0));
		_ARM9labels.add(new DSILabel("Gamecard_Encryption_Seed_1_Lower_32bit", 0x40001B4));
		_ARM9labels.add(new DSILabel("Gamecard_Encryption_Seed_0_Upper_7bit", 0x40001B8));
		_ARM9labels.add(new DSILabel("Gamecard_Encryption_Seed_1_Upper_7bit", 0x40001BA));

		//ARM9 Memory and IRQ Control
		_ARM9labels.add(new DSILabel("REG_EXMEMCNT", 0x4000204));
		_ARM9labels.add(new DSILabel("REG_IME", 0x4000208));
		_ARM9labels.add(new DSILabel("REG_IE", 0x4000210));
		_ARM9labels.add(new DSILabel("REG_IF", 0x4000214));
		_ARM9labels.add(new DSILabel("VRAMCNT_A", 0x4000240));
		_ARM9labels.add(new DSILabel("VRAMCNT_B", 0x4000241));
		_ARM9labels.add(new DSILabel("VRAMCNT_C", 0x4000242));
		_ARM9labels.add(new DSILabel("VRAMCNT_D", 0x4000243));
		_ARM9labels.add(new DSILabel("VRAMCNT_E", 0x4000244));
		_ARM9labels.add(new DSILabel("VRAMCNT_F", 0x4000245));
		_ARM9labels.add(new DSILabel("VRAMCNT_G", 0x4000246));
		_ARM9labels.add(new DSILabel("WRAMCNT", 0x4000247));
		_ARM9labels.add(new DSILabel("VRAMCNT_H", 0x4000248));
		_ARM9labels.add(new DSILabel("VRAMCNT_I", 0x4000249));
		
		//ARM9 Maths
		_ARM9labels.add(new DSILabel("REG_DIVCNT", 0x4000280));
		_ARM9labels.add(new DSILabel("REG_DIV_NUMER", 0x4000290));
		_ARM9labels.add(new DSILabel("REG_DIV_DENOM", 0x4000298));
		_ARM9labels.add(new DSILabel("REG_DIV_RESULT", 0x40002A0));
		_ARM9labels.add(new DSILabel("REG_DIVREM_RESULT", 0x40002A8));
		_ARM9labels.add(new DSILabel("REG_SQRTCNT", 0x40002B0));
		_ARM9labels.add(new DSILabel("SQRT_RESULT", 0x40002B4));
		_ARM9labels.add(new DSILabel("SQRT_PARAM", 0x40002B8));
		_ARM9labels.add(new DSILabel("POSTFLG", 0x400030));
		_ARM9labels.add(new DSILabel("POWCNT1", 0x4000304));

		//ARM9 3D Display Engine
		_ARM9labels.add(new DSILabel("3d_Engine", 0x4000320));
	
		//ARM9 Display Engine B	
		_ARM9labels.add(new DSILabel("REG_DISPCNT", 0x4001000));
		_ARM9labels.add(new DSILabel("REG_DISPSTAT", 0x4001004));
		_ARM9labels.add(new DSILabel("REG_MASTER_BRIGHT", 0x4001006));

		//DSi extra registers: not included in NTRGhidra (stay tuned for TWLGhidra)
		
		//ARM9 IPC/ROM
		_ARM9labels.add(new DSILabel("IPCFIFORECV", 0x4100000));
		_ARM9labels.add(new DSILabel("Gamecard_bus_4_byte_data_in,for_manual_or_dma_read", 0x4100010));
	
		//ARM9 DS Debug Registers (Emulator/Devkits) 
		_ARM9labels.add(new DSILabel("Start_of_Ensata_Emulator_Debug_Registers", 0x4FFF000));
		_ARM9labels.add(new DSILabel("Start_of_NoSgba_Emulator_Debug_Registers", 0x4FFFA00));
		
		//ARM9 Hardcoded RAM Addresses for Exception Handling
		_ARM9labels.add(new DSILabel("NDS9_Debug_Stacktop_Debug_Vector", 0x27FFD9C));
		//See gbatek for these 2:
		//_ARM9labels.add(new DSILabel("NDS9 IRQ Check Bits (hardcoded RAM address)", 0x4FFFA00));
		//_ARM9labels.add(new DSILabel("NDS9 IRQ Handler (hardcoded RAM address)", 0x4FFF000));

		//Main Memory Control
		_ARM9labels.add(new DSILabel("Main_Memory_Control", 0x27FFFFE));
		
		
		
	
		
		/*ARM7*/
		
		//I/O Map
		_ARM7labels.add(new DSILabel("REG_DISPSTAT", 0x4000004));
		_ARM7labels.add(new DSILabel("REG_VCOUNT", 0x4000006));
		_ARM7labels.add(new DSILabel("DMA_Channels_0_to_3", 0x40000B0));
		_ARM7labels.add(new DSILabel("Timers_0_to_3", 0x4000100));
		_ARM7labels.add(new DSILabel("Debug_SIODATA32", 0x4000120));
		_ARM7labels.add(new DSILabel("Debug_SIOCNT", 0x4000128));
		_ARM7labels.add(new DSILabel("REG_keyinput", 0x4000130));
		_ARM7labels.add(new DSILabel("REG_keycnt", 0x4000132));
		_ARM7labels.add(new DSILabel("REG_Debug_RCNT", 0x4000134));
		_ARM7labels.add(new DSILabel("REG_EXTKEYIN", 0x4000136));
		_ARM7labels.add(new DSILabel("RTC_Realtime_Clock_Bus", 0x4000138));
		_ARM7labels.add(new DSILabel("REG_IPCSYNC", 0x4000180));
		_ARM7labels.add(new DSILabel("REG_IPCFIFOCNT", 0x4000184));
		_ARM7labels.add(new DSILabel("IPCFIFOSEND", 0x4000188));
		_ARM7labels.add(new DSILabel("REG_AUXSPICNT", 0x40001A0));
		_ARM7labels.add(new DSILabel("REG_AUXSPIDATA", 0x40001A2));
		_ARM7labels.add(new DSILabel("Gamecard_bus_timing_control", 0x40001A4));
		_ARM7labels.add(new DSILabel("Gamecard_bus_8_byte_command_out", 0x40001A8));
		_ARM7labels.add(new DSILabel("Gamecard_Encryption_Seed_0_Lower_32bit", 0x40001B0));
		_ARM7labels.add(new DSILabel("Gamecard_Encryption_Seed_1_Lower_32bit", 0x40001B4));
		_ARM7labels.add(new DSILabel("Gamecard_Encryption_Seed_0_Upper_7bit_(bit7-15_unused)", 0x40001B8));
		_ARM7labels.add(new DSILabel("Gamecard_Encryption_Seed_1_Upper_7bit_(bit7-15_unused)", 0x40001BA));
		_ARM7labels.add(new DSILabel("SPI_bus_Control", 0x40001C0));
		_ARM7labels.add(new DSILabel("SPI_bus_Data", 0x40001C2));
	
		//ARM7 Memory and IRQ Control
		_ARM7labels.add(new DSILabel("REG_EXMEMSTAT", 0x4000204));
		_ARM7labels.add(new DSILabel("REG_WIFIWAITCNT", 0x4000206));
		_ARM7labels.add(new DSILabel("REG_IME", 0x4000208));
		_ARM7labels.add(new DSILabel("REG_IE", 0x4000210));
		_ARM7labels.add(new DSILabel("REG_IF", 0x4000214));
		_ARM7labels.add(new DSILabel("REG_IE2", 0x4000218));
		_ARM7labels.add(new DSILabel("REG_IF2", 0x400021C));
		_ARM7labels.add(new DSILabel("REG_VRAMSTAT", 0x4000240));
		_ARM7labels.add(new DSILabel("REG_WRAMSTAT", 0x4000241));
		_ARM7labels.add(new DSILabel("REG_POSTFLG", 0x4000300));
		_ARM7labels.add(new DSILabel("REG_HALTCNT", 0x4000301));
		_ARM7labels.add(new DSILabel("REG_POWCNT2", 0x4000304));
		_ARM7labels.add(new DSILabel("REG_BIOSPROT", 0x4000308));
		
		//ARM7 Sound Registers (NOTE: sound channle specific registers missing)
		_ARM7labels.add(new DSILabel("Sound_Channels_0_to_15", 0x4000400));
		//...
		_ARM7labels.add(new DSILabel("REG_SOUNDCNT", 0x4000500));
		_ARM7labels.add(new DSILabel("REG_SOUNDBIAS", 0x4000504));
		_ARM7labels.add(new DSILabel("REG_SNDCAP0CNT", 0x4000508));
		_ARM7labels.add(new DSILabel("REG_SNDCAP1CNT", 0x4000509));
		_ARM7labels.add(new DSILabel("REG_SNDCAP0DAD", 0x4000510));
		_ARM7labels.add(new DSILabel("REG_SNDCAP0LEN", 0x4000514));
		_ARM7labels.add(new DSILabel("REG_SNDCAP1DAD", 0x4000518));
		_ARM7labels.add(new DSILabel("REG_SNDCAP1LEN", 0x400051C));
		
		//ARM7 DSi Extra Registers
		//not included
		
		//ARM7 IPC/ROM
		_ARM7labels.add(new DSILabel("IPCFIFORECV", 0x4100000));
		_ARM7labels.add(new DSILabel("Gamecard_bus_4_byte_data_in,for_manual_or_dma_read", 0x4100010));
		
		//ARM7 3DS
		//not included
		
		//ARM7 WLAN Registers
		_ARM7labels.add(new DSILabel("Wifi_WS0_Region", 0x4800000));
		_ARM7labels.add(new DSILabel("Wifi_WS1_Region", 0x4808000));
		
		//ARM7 Hardcoded RAM Addresses for Exception Handling
		_ARM7labels.add(new DSILabel("DSi7_IRQ_IF2_Check_Bits_DSi_only)", 0x380FFC0));
		_ARM7labels.add(new DSILabel("NDS7_Debug_Stacktop_Debug_Vector", 0x380FFDC));
		_ARM7labels.add(new DSILabel("NDS7_IRQ_IF_Check_Bits", 0x380FFF8));
		_ARM7labels.add(new DSILabel("NDS7_IRQ_Handler", 0x380FFFC));
	
	}
	
	// Singleton pattern 
	public static DSILabelList getInstance()
	{
		if(_instance==null)
			_instance=new DSILabelList();
		
		return _instance;
	}
	
	public List<DSILabel> getARM9Labels()
	{
		return _ARM9labels;
	}
	
	public List<DSILabel> getARM7Labels()
	{
		return _ARM7labels;
	}
	
}
