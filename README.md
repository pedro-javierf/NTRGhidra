# NTRGhidra
A Nintendo DS Loader for Ghidra


![NTRGhidra a Nintendo DS Loader for Ghidra](https://media.defense.gov/2019/Apr/04/2002109557/-1/-1/0/190404-D-IM742-1002.PNG)

# License
NTRGhidra is released under the ![Apache License 2.0](https://github.com/pedro-javierf/NTRGhidra/blob/master/LICENSE).

# Installation

If you just want to install the loader into a existing Ghidra installation:

1. Download the .zip from releases OR build the project.
2. Put the .zip into the GHIDRA_INSTALL_DIR/Extensions/Ghidra folder
3. In the initial window (not the Code Browser), open the File menu, and select Install Extensions. Click the small 'plus' icon in the top right of the window, and select the extension zip file downloaded. This should add an entry into the extensions list. Make sure it is checked and click OK.
![Step 1](install1.png)
![Step 2](install2.PNG)
4. Restart Ghidra.

# Source and Build

Provided is an Eclipse project to debug and build the loader. You must have a Ghidra installation as well as the Ghidra Eclipse extension.

To export a build of the project in Eclipse select File > Export and then choose Ghidra > Ghidra Module Extension

# Features
 - [x] Load NTR format files (usually .nds extension).
 - [x] Can decompress Nintendo SDK ARM9 code!
 - [x] Memory Map support (I/O memory with names)
-  [x] Overlay Loading Support

# Work in progress features
 - [ ] SDK autoloading
 - [ ] DSi (TWL) Support


# Acknowledgements
Thanks to Gericom for the original C# Header and ARM9 decompression code from EFE(Every File Explorer).
