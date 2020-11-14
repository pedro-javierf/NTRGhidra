package ntrghidra;

import java.util.ArrayList;
import java.util.List;

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
			_ARM9labels.add(new NDSLabel("Gamecard_bus_timing_control", 0x40001A4));
			_ARM9labels.add(new NDSLabel("Gamecard_bus_8_byte_command_out", 0x40001A8));
			_ARM9labels.add(new NDSLabel("Gamecard_Encryption_Seed_0_Lower_32bit", 0x40001B0));
			_ARM9labels.add(new NDSLabel("Gamecard_Encryption_Seed_1_Lower_32bit", 0x40001B4));
			_ARM9labels.add(new NDSLabel("Gamecard_Encryption_Seed_0_Upper_7bit", 0x40001B8));
			_ARM9labels.add(new NDSLabel("Gamecard_Encryption_Seed_1_Upper_7bit", 0x40001BA));

			//ARM9 Memory and IRQ Control
			_ARM9labels.add(new NDSLabel("REG_EXMEMCNT", 0x4000204));
			_ARM9labels.add(new NDSLabel("REG_IME", 0x4000208));
			_ARM9labels.add(new NDSLabel("REG_IE", 0x4000210));
			_ARM9labels.add(new NDSLabel("REG_IF", 0x4000214));
			_ARM9labels.add(new NDSLabel("VRAMCNT_A", 0x4000240));
			_ARM9labels.add(new NDSLabel("VRAMCNT_B", 0x4000241));
			_ARM9labels.add(new NDSLabel("VRAMCNT_C", 0x4000242));
			_ARM9labels.add(new NDSLabel("VRAMCNT_D", 0x4000243));
			_ARM9labels.add(new NDSLabel("VRAMCNT_E", 0x4000244));
			_ARM9labels.add(new NDSLabel("VRAMCNT_F", 0x4000245));
			_ARM9labels.add(new NDSLabel("VRAMCNT_G", 0x4000246));
			_ARM9labels.add(new NDSLabel("WRAMCNT", 0x4000247));
			_ARM9labels.add(new NDSLabel("VRAMCNT_H", 0x4000248));
			_ARM9labels.add(new NDSLabel("VRAMCNT_I", 0x4000249));
			
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
			_ARM9labels.add(new NDSLabel("3d_Engine", 0x4000320));
		
			//ARM9 Display Engine B	
			_ARM9labels.add(new NDSLabel("REG_DISPCNT", 0x4001000));
			_ARM9labels.add(new NDSLabel("REG_DISPSTAT", 0x4001004));
			_ARM9labels.add(new NDSLabel("REG_MASTER_BRIGHT", 0x4001006));

			//DSi extra registers: not included in NTRGhidra (stay tuned for TWLGhidra)
			
			//ARM9 IPC/ROM
			_ARM9labels.add(new NDSLabel("IPCFIFORECV", 0x4100000));
			_ARM9labels.add(new NDSLabel("Gamecard_bus_4_byte_data_in,for_manual_or_dma_read", 0x4100010));
		
			//ARM9 DS Debug Registers (Emulator/Devkits) 
			_ARM9labels.add(new NDSLabel("Start_of_Ensata_Emulator_Debug_Registers", 0x4FFF000));
			_ARM9labels.add(new NDSLabel("Start_of_NoSgba_Emulator_Debug_Registers", 0x4FFFA00));
			
			//ARM9 Hardcoded RAM Addresses for Exception Handling
			_ARM9labels.add(new NDSLabel("NDS9_Debug_Stacktop_Debug_Vector", 0x27FFD9C));
			//See gbatek for these 2:
			//_ARM9labels.add(new NDSLabel("NDS9 IRQ Check Bits (hardcoded RAM address)", 0x4FFFA00));
			//_ARM9labels.add(new NDSLabel("NDS9 IRQ Handler (hardcoded RAM address)", 0x4FFF000));

			//Main Memory Control
			_ARM9labels.add(new NDSLabel("Main_Memory_Control", 0x27FFFFE));
			
			
			
			
			
			/*ARM7*/
			
			//I/O Map
			_ARM7labels.add(new NDSLabel("REG_DISPSTAT", 0x4000004));
			_ARM7labels.add(new NDSLabel("REG_VCOUNT", 0x4000006));
			_ARM7labels.add(new NDSLabel("DMA_Channels_0_to_3", 0x40000B0));
			_ARM7labels.add(new NDSLabel("Timers_0_to_3", 0x4000100));
			_ARM7labels.add(new NDSLabel("Debug_SIODATA32", 0x4000120));
			_ARM7labels.add(new NDSLabel("Debug_SIOCNT", 0x4000128));
			_ARM7labels.add(new NDSLabel("REG_keyinput", 0x4000130));
			_ARM7labels.add(new NDSLabel("REG_keycnt", 0x4000132));
			_ARM7labels.add(new NDSLabel("REG_Debug_RCNT", 0x4000134));
			_ARM7labels.add(new NDSLabel("REG_EXTKEYIN", 0x4000136));
			_ARM7labels.add(new NDSLabel("RTC_Realtime_Clock_Bus", 0x4000138));
			_ARM7labels.add(new NDSLabel("REG_IPCSYNC", 0x4000180));
			_ARM7labels.add(new NDSLabel("REG_IPCFIFOCNT", 0x4000184));
			_ARM7labels.add(new NDSLabel("IPCFIFOSEND", 0x4000188));
			_ARM7labels.add(new NDSLabel("REG_AUXSPICNT", 0x40001A0));
			_ARM7labels.add(new NDSLabel("REG_AUXSPIDATA", 0x40001A2));
			_ARM7labels.add(new NDSLabel("Gamecard_bus_timing_control", 0x40001A4));
			_ARM7labels.add(new NDSLabel("Gamecard_bus_8_byte_command_out", 0x40001A8));
			_ARM7labels.add(new NDSLabel("Gamecard_Encryption_Seed_0_Lower_32bit", 0x40001B0));
			_ARM7labels.add(new NDSLabel("Gamecard_Encryption_Seed_1_Lower_32bit", 0x40001B4));
			_ARM7labels.add(new NDSLabel("Gamecard_Encryption_Seed_0_Upper_7bit_(bit7-15_unused)", 0x40001B8));
			_ARM7labels.add(new NDSLabel("Gamecard_Encryption_Seed_1_Upper_7bit_(bit7-15_unused)", 0x40001BA));
			_ARM7labels.add(new NDSLabel("SPI_bus_Control", 0x40001C0));
			_ARM7labels.add(new NDSLabel("SPI_bus_Data", 0x40001C2));
		
			//ARM7 Memory and IRQ Control
			_ARM7labels.add(new NDSLabel("REG_EXMEMSTAT", 0x4000204));
			_ARM7labels.add(new NDSLabel("REG_WIFIWAITCNT", 0x4000206));
			_ARM7labels.add(new NDSLabel("REG_IME", 0x4000208));
			_ARM7labels.add(new NDSLabel("REG_IE", 0x4000210));
			_ARM7labels.add(new NDSLabel("REG_IF", 0x4000214));
			_ARM7labels.add(new NDSLabel("REG_IE2", 0x4000218));
			_ARM7labels.add(new NDSLabel("REG_IF2", 0x400021C));
			_ARM7labels.add(new NDSLabel("REG_VRAMSTAT", 0x4000240));
			_ARM7labels.add(new NDSLabel("REG_WRAMSTAT", 0x4000241));
			_ARM7labels.add(new NDSLabel("REG_POSTFLG", 0x4000300));
			_ARM7labels.add(new NDSLabel("REG_HALTCNT", 0x4000301));
			_ARM7labels.add(new NDSLabel("REG_POWCNT2", 0x4000304));
			_ARM7labels.add(new NDSLabel("REG_BIOSPROT", 0x4000308));
			
			
			//ARM7 Sound Registers (NOTE: sound channle specific registers missing)
			_ARM7labels.add(new NDSLabel("Sound_Channels_0_to_15", 0x4000400));
			//...
			_ARM7labels.add(new NDSLabel("REG_SOUNDCNT", 0x4000500));
			_ARM7labels.add(new NDSLabel("REG_SOUNDBIAS", 0x4000504));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP0CNT", 0x4000508));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP1CNT", 0x4000509));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP0DAD", 0x4000510));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP0LEN", 0x4000514));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP1DAD", 0x4000518));
			_ARM7labels.add(new NDSLabel("REG_SNDCAP1LEN", 0x400051C));
			
			
			//ARM7 DSi Extra Registers
			//not included
			
			//ARM7 IPC/ROM
			_ARM7labels.add(new NDSLabel("IPCFIFORECV", 0x4100000));
			_ARM7labels.add(new NDSLabel("Gamecard_bus_4_byte_data_in,for_manual_or_dma_read", 0x4100010));
			
			//ARM7 3DS
			//not included
			
			//ARM7 WLAN Registers
			_ARM7labels.add(new NDSLabel("Wifi_WS0_Region", 0x4800000));
			_ARM7labels.add(new NDSLabel("Wifi_WS1_Region", 0x4808000));
			
			
			//ARM7 Hardcoded RAM Addresses for Exception Handling
			_ARM7labels.add(new NDSLabel("DSi7_IRQ_IF2_Check_Bits_DSi_only)", 0x380FFC0));
			_ARM7labels.add(new NDSLabel("NDS7_Debug_Stacktop_Debug_Vector", 0x380FFDC));
			_ARM7labels.add(new NDSLabel("NDS7_IRQ_IF_Check_Bits", 0x380FFF8));
			_ARM7labels.add(new NDSLabel("NDS7_IRQ_Handler", 0x380FFFC));
		
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
