# NTRGhidra 

A Nintendo DS Loader and plugin for Ghidra
<img width="2048" height="512" alt="NTRGhidra Logo" src="https://github.com/user-attachments/assets/aec375d3-3cc9-45cd-9df3-32c076ec68f4" />

Latest Ghidra version supported: 12.0.4 (09/03/2026 | dd/mm/yyyy)

[![Build NTRGhidra](https://github.com/pedro-javierf/NTRGhidra/actions/workflows/build_ntrghidra.yml/badge.svg)](https://github.com/pedro-javierf/NTRGhidra/actions/workflows/build_ntrghidra.yml) ![](https://img.shields.io/github/issues/pedro-javierf/NTRGhidra/bug)<br>

# Installation
If you just want to install the loader into a existing Ghidra installation:

1. Download the .zip from the [releases page](https://github.com/pedro-javierf/NTRGhidra/releases) (Or build the project yourself from the code, instructions below).
2. Put the .zip into the GHIDRA_INSTALL_DIR/Extensions/Ghidra folder
3. In the initial Ghidra window (not in the Code Browser), open the "File" menu, and select "Install Extensions". Click the small 'plus' icon in the top right of the window, and select the extension zip file downloaded. This should add an entry into the extensions list. Make sure it is checked and click OK.

![Step 1](install1.png)
![Step 2](install2.PNG)

4. Restart Ghidra.

# How to Build yourself

NOTE: If you notice any exception while debugging with Eclipse, please ![create an issue](https://github.com/pedro-javierf/NTRGhidra/issues)

These are the basic steps to debug and build the loader. You must have a Ghidra installation.

1. Install Ghidra
2. Install the Eclipse IDE
3. In Eclipse, install the Ghidra Development Extension. Click Help > "Install New Software" (https://stackoverflow.com/questions/31553376/eclipse-how-to-install-a-plugin-manually)
4. After the extension is installed, clone/download this repository, which contains an Eclipse project.
5. Open the project with Eclipse (File > Open Projects from File System)
6. You may need to reconnect Ghidra to the project by right clicking the project in Eclipse, and doing GhidraDev -> Link Ghidra...
7. Build: Select "File > Export", and then choose "Ghidra > Ghidra Module Extension". You can then use a local Gradle installation or an online build system.


# License
NTRGhidra is released under the ![Apache License 2.0](https://github.com/pedro-javierf/NTRGhidra/blob/master/LICENSE).

# Contributing & Support
* Fork, modify and pull request to contribute,
* Open issues suggesting feautures, reporting bugs, asking for documentation or changes, etc.
* Solve known problems reported in the ![issues](https://github.com/pedro-javierf/NTRGhidra/issues) 
* Support by starring the project.

```diff
! This project is looking for maintainers and contributors --> If you'd like to join this effort
! and contribute or administer the project in any way, please open an issue to get in touch. Thank you!
```

# Features
A list of In Progress / To-Do / Completed features is available in [this listing](https://github.com/pedro-javierf/NTRGhidra/projects/1)

## Dynamic Overlay Loading example
![](https://private-user-images.githubusercontent.com/2887824/556133673-1ebfc653-ac13-48a0-a3c1-8a9849ce8d0c.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzMwNzY3NDAsIm5iZiI6MTc3MzA3NjQ0MCwicGF0aCI6Ii8yODg3ODI0LzU1NjEzMzY3My0xZWJmYzY1My1hYzEzLTQ4YTAtYTNjMS04YTk4NDljZThkMGMucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQVZDT0RZTFNBNTNQUUs0WkElMkYyMDI2MDMwOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNjAzMDlUMTcxNDAwWiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9OWQ3NGMzMDJjNTg5ZDY5MDQ2NGYyOWIyZjM4OWZhMTc2MjZjNWYwZWVhMDM2NTg0ZTA3ZTQzYzY5YzdmZWFkNiZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QifQ.TznDMaHZ6x3UrMJUp5QVeNW5H_oU80SzaR1dpLvUCw8)

# Acknowledgements
* Special thanks to [gbatek](https://problemkaputt.de/gbatek.htm) for ds technical info
* Thanks to Gericom for the original C# Header and ARM9 decompression code from EFE(Every File Explorer), as well as for testing.

# Others
For reference and tutorials about Ghidra loaders and how to write them, you may want to read:
* [Tutorial: Writing a Ghidra loader](https://pedro-javierf.github.io/devblog/tutorialwritingaghidraloader/)
* [Advanced Ghidra Loader: labels, overlays and Function ID](https://pedro-javierf.github.io/devblog/advancedghidraloader/)
