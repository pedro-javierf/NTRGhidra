# NTRGhidra
A Nintendo DS Loader for Ghidra


![NTRGhidra a Nintendo DS Loader for Ghidra](https://media.defense.gov/2019/Apr/04/2002109557/-1/-1/0/190404-D-IM742-1002.PNG)


# Installation

If you just want to install the loader into a existing Ghidra installation:

1. Download the .zip from releases OR build the project.
2. Put the .zip into the GHIDRA_INSTALL_DIR/Extensions/Ghidra folder
3. In the initial window (not the Code Browser), open the File menu, and select Install Extensions. Click the small 'plus' icon in the top right of the window, and select the extension zip file downloaded. This should add an entry into the extensions list. Make sure it is checked and click OK.
4. Restart Ghidra.

# Source and Build

Provided is an Eclipse project to debug and build the loader. You must have a Ghidra installation as well as the Ghidra Eclipse extension.

# Features
 - [x] Load NTR format files (usually .nds extension). At the moment ARM7 support is perfect but ARM9 requires decompression to be really useful.
 
# Work in progress features
 - [ ] SDK Decompression (almost there)
 - [ ] SDK autoloading
 
# Todo
 - [ ] Multiple language support for dialogs
 - [x] Add IO memory with names
 - [ ] Function signatures for nitro/twl sdk functions
