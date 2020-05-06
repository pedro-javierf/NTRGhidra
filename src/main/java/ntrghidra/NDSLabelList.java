package ntrghidra;

import java.util.ArrayList;
import java.util.List;

import ntrghidra.NDSMemRegionList.NDSMemRegion;

public class NDSLabelList {

	//This internal class represents a location of interest (registers, i/o, etc)
		public class NDSLabel {
		    String name;
		    int addr;

		    public NDSLabel(String name, int addr) {
		            this.name = name;
		            this.addr = addr;
		    }
		    public String name() {return name;}
		    public int addr() {return addr;}

		}
	
		//This class uses a singleton pattern
		private static NDSLabelList _instance = null;
		private static List<NDSLabel> _ARM9labels = new ArrayList<NDSLabel>();
		private static List<NDSLabel> _ARM7labels = new ArrayList<NDSLabel>();

		
		//https://problemkaputt.de/gbatek.htm#dsiomaps
		private NDSLabelList()
		{	
			/*ARM 9*/
			//ARM9 Display Engine A
			_ARM9labels.add(new NDSLabel("REG_DISPCNT", 0x4000000));
			_ARM9labels.add(new NDSLabel("REG_DISPSTAT", 0x4000004));
			_ARM9labels.add(new NDSLabel("REG_VCOUNT", 0x4000006));
			_ARM9labels.add(new NDSLabel("2D_ENGINE_A", 0x4000008));
			_ARM9labels.add(new NDSLabel("REG_DISP3DCNT", 0x4000060));
			_ARM9labels.add(new NDSLabel("REG_DISPCAPCNT", 0x4000064));
			_ARM9labels.add(new NDSLabel("REG_DISP_MMEM_FIFO", 0x4000068));
			_ARM9labels.add(new NDSLabel("REG_MASTER_BRIGHT", 0x400006C));
			
			//ARM9 DMA, Timers, and Keypad
			_ARM9labels.add(new NDSLabel("DMA_CHANNEL_0_to_3", 0x40000B0));
			_ARM9labels.add(new NDSLabel("DMA_FILL", 0x40000E0));
			_ARM9labels.add(new NDSLabel("TIMERS", 0x4000100));
			_ARM9labels.add(new NDSLabel("REG_KEYINPUT", 0x4000130));
			_ARM9labels.add(new NDSLabel("REG_KEYCNT", 0x4000132));
			
			//ARM9 IPC/ROM
			_ARM9labels.add(new NDSLabel("REG_IPCSYNC", 0x4000180));
			_ARM9labels.add(new NDSLabel("REG_IPCFIFOCNT", 0x4000184));
			_ARM9labels.add(new NDSLabel("REG_IPCFIFOSEND", 0x4000188));
			_ARM9labels.add(new NDSLabel("REG_AUXSPICNT", 0x40001A0));
			_ARM9labels.add(new NDSLabel("REG_AUXSPIDATA", 0x40001A2));
			_ARM9labels.add(new NDSLabel("Gamecard bus timing/control", 0x40001A4));
			_ARM9labels.add(new NDSLabel("Gamecard bus 8-byte command out", 0x40001A8));
			_ARM9labels.add(new NDSLabel("Gamecard Encryption Seed 0 Lower 32bit", 0x40001B0));
			_ARM9labels.add(new NDSLabel("Gamecard Encryption Seed 1 Lower 32bit", 0x40001B4));
			_ARM9labels.add(new NDSLabel("Gamecard Encryption Seed 0 Upper 7bit (bit7-15 unused)", 0x40001B8));
			_ARM9labels.add(new NDSLabel("Gamecard Encryption Seed 1 Upper 7bit (bit7-15 unused)", 0x40001BA));

			//ARM9 Memory and IRQ Control
			_ARM9labels.add(new NDSLabel("REG_EXMEMCNT", 0x4000204));
			_ARM9labels.add(new NDSLabel("REG_IME - Interrupt Master Enable", 0x4000208));
			_ARM9labels.add(new NDSLabel("REG_IE  - Interrupt Enable", 0x4000210));
			_ARM9labels.add(new NDSLabel("REG_IF  - Interrupt Request Flags", 0x4000214));
			_ARM9labels.add(new NDSLabel("VRAMCNT_A - VRAM-A (128K) Bank Control", 0x4000240));
			_ARM9labels.add(new NDSLabel("VRAMCNT_B - VRAM-B (128K) Bank Control", 0x4000241));
			_ARM9labels.add(new NDSLabel("VRAMCNT_C - VRAM-C (128K) Bank ControlL", 0x4000242));
			_ARM9labels.add(new NDSLabel("VRAMCNT_D - VRAM-D (128K) Bank Control", 0x4000243));
			_ARM9labels.add(new NDSLabel("VRAMCNT_E - VRAM-E (64K) Bank Control", 0x4000244));
			_ARM9labels.add(new NDSLabel("VRAMCNT_F - VRAM-F (16K) Bank Control", 0x4000245));
			_ARM9labels.add(new NDSLabel("VRAMCNT_G - VRAM-G (16K) Bank Control", 0x4000246));
			_ARM9labels.add(new NDSLabel("WRAMCNT   - WRAM Bank Control", 0x4000247));
			_ARM9labels.add(new NDSLabel("VRAMCNT_H - VRAM-H (32K) Bank Control", 0x4000248));
			_ARM9labels.add(new NDSLabel("VRAMCNT_I - VRAM-I (16K) Bank Control", 0x4000249));
			
			//ARM9 Maths
			_ARM9labels.add(new NDSLabel("REG_DIVCNT", 0x4000280));
			_ARM9labels.add(new NDSLabel("REG_DIV_NUMER", 0x4000290));
			_ARM9labels.add(new NDSLabel("REG_DIV_DENOM", 0x4000298));
			_ARM9labels.add(new NDSLabel("REG_DIV_RESULT", 0x40002A0));
			_ARM9labels.add(new NDSLabel("REG_DIVREM_RESULT", 0x40002A8));
			_ARM9labels.add(new NDSLabel("REG_SQRTCNT", 0x40002B0));
			_ARM9labels.add(new NDSLabel("SQRT_RESULT", 0x40002B4));
			_ARM9labels.add(new NDSLabel("SQRT_PARAM", 0x40002B8));
			_ARM9labels.add(new NDSLabel("POSTFLG", 0x400030));
			_ARM9labels.add(new NDSLabel("POWCNT1", 0x4000304));

			//ARM9 3D Display Engine
			_ARM9labels.add(new NDSLabel("3d Engine", 0x4000320));
		
			//ARM9 Display Engine B	
			_ARM9labels.add(new NDSLabel("REG_DISPCNT", 0x4001000));
			_ARM9labels.add(new NDSLabel("REG_DISPSTAT", 0x4001004));
			_ARM9labels.add(new NDSLabel("REG_MASTER_BRIGHT", 0x4001006));

			//DSi extra registers: not included in NTRGhidra (stay tuned for TWLGhidra)
			
			//ARM9 IPC/ROM
			_ARM9labels.add(new NDSLabel("IPCFIFORECV", 0x4100000));
			_ARM9labels.add(new NDSLabel("Gamecard bus 4-byte data in, for manual or dma read", 0x4100010));
		
			//ARM9 DS Debug Registers (Emulator/Devkits) 
			_ARM9labels.add(new NDSLabel("Start of Ensata Emulator Debug Registers", 0x4FFF000));
			_ARM9labels.add(new NDSLabel("Start of No$gba Emulator Debug Registers", 0x4FFFA00));
			
			//ARM9 Hardcoded RAM Addresses for Exception Handling
			_ARM9labels.add(new NDSLabel("NDS9 Debug Stacktop / Debug Vector (0=None)", 0x27FFD9C));
			//See gbatek for these 2:
			//_ARM9labels.add(new NDSLabel("NDS9 IRQ Check Bits (hardcoded RAM address)", 0x4FFFA00));
			//_ARM9labels.add(new NDSLabel("NDS9 IRQ Handler (hardcoded RAM address)", 0x4FFF000));

			//Main Memory Control
			_ARM9labels.add(new NDSLabel("Main Memory Control", 0x27FFFFE));
			
			
			
			
			
			/*ARM7*/
		}
		
		public static NDSLabelList getInstance()
		{
			if(_instance==null)
				_instance=new NDSLabelList();
			
			return _instance;
		}
		
		public List<NDSLabel> getARM9Labels()
		{
			return _ARM9labels;
		}
		
		public List<NDSLabel> getARM7Labels()
		{
			return _ARM7labels;
		}
		
}
