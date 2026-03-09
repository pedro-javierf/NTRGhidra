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

4. If you wish to dynamically load/unload overlays, you must activate this feature by activating the plugin features, using `File` -> `Configure` -> `Miscellaneous` -> `NTRGhidraPlugin` and enable NTRGhidraPlugin.

![Enable plugin for overlays feature](https://private-user-images.githubusercontent.com/2887824/557116007-b15ffc90-9024-41ef-87f7-34021bcd9fb4.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzMwNzc1NTYsIm5iZiI6MTc3MzA3NzI1NiwicGF0aCI6Ii8yODg3ODI0LzU1NzExNjAwNy1iMTVmZmM5MC05MDI0LTQxZWYtODdmNy0zNDAyMWJjZDlmYjQucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQVZDT0RZTFNBNTNQUUs0WkElMkYyMDI2MDMwOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNjAzMDlUMTcyNzM2WiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9YWRmZTNlNzgzOWVkNTI3OWY0Mjc4NTQ3MTE0YThhNTUzNTE4MmFhZDE3OTgzYjRiMTAxMzdhNWY2MGJmNDRlZiZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QifQ.txHOvG1-q8yjk3Ib6oEeD3MLHooI3DrEXp57ek1hkhQ)

5. Restart Ghidra.

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

Enable Plugin using `File` -> `Configure` -> `Miscellaneous` -> `NTRGhidraPlugin` before using:
![](https://private-user-images.githubusercontent.com/2887824/557116007-b15ffc90-9024-41ef-87f7-34021bcd9fb4.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzMwNzcxMjYsIm5iZiI6MTc3MzA3NjgyNiwicGF0aCI6Ii8yODg3ODI0LzU1NzExNjAwNy1iMTVmZmM5MC05MDI0LTQxZWYtODdmNy0zNDAyMWJjZDlmYjQucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQVZDT0RZTFNBNTNQUUs0WkElMkYyMDI2MDMwOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNjAzMDlUMTcyMDI2WiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9ZGNiYzM3MTUwNjcxOTExOWVhMTQ4YjFmMjg4Y2Q2YWFhMzY0MmZlYTkxYTA1ZWFjNTJmOTA5NzRlZGM5Mjc5NyZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QifQ.gswpe3d-vAvEICJI1VTbeEll2gkICfWtc3vx_ogQdGk)

Selecting desired overlays after loading:

(**Note**: this REQUIRES the program's original ROM file to be in the exact same file path as when it was imported into Ghidra. )
![](https://private-user-images.githubusercontent.com/2887824/557114924-3337acc9-9696-4f35-b179-c3d18382cd37.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzMwNzcxMjYsIm5iZiI6MTc3MzA3NjgyNiwicGF0aCI6Ii8yODg3ODI0LzU1NzExNDkyNC0zMzM3YWNjOS05Njk2LTRmMzUtYjE3OS1jM2QxODM4MmNkMzcucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQVZDT0RZTFNBNTNQUUs0WkElMkYyMDI2MDMwOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNjAzMDlUMTcyMDI2WiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9ZWI4OTAyNDA2MGFlNTFiYmY1Mjk2ZTM0YzYyOGExMjdjN2VjYWU2ZWYwMjA4NGY4MDk2NDM2ZjMwMGRlNDFmOSZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QifQ.xI9EtxtAv9UacFcyoi8oYQtdgYCK5RipD9S3mW9Mlzc)
![](https://private-user-images.githubusercontent.com/2887824/557114983-7625d06e-2302-48bc-8b3c-49fc743fd6be.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NzMwNzcxMjYsIm5iZiI6MTc3MzA3NjgyNiwicGF0aCI6Ii8yODg3ODI0LzU1NzExNDk4My03NjI1ZDA2ZS0yMzAyLTQ4YmMtOGIzYy00OWZjNzQzZmQ2YmUucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9QUtJQVZDT0RZTFNBNTNQUUs0WkElMkYyMDI2MDMwOSUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNjAzMDlUMTcyMDI2WiZYLUFtei1FeHBpcmVzPTMwMCZYLUFtei1TaWduYXR1cmU9MDY3ZGJlMzY4MjAyZDAyNTRkNjNiMDI4YTkyZGY2OGRjZDc3NTIzZGNiYThmZjI1YzIyZWU4YzgxMTY5MmIwNyZYLUFtei1TaWduZWRIZWFkZXJzPWhvc3QifQ.gS02wK9I6ondz31jFPFNg5KTsxju4LbC6pm4tJGskCo)

# Acknowledgements
* Special thanks to [gbatek](https://problemkaputt.de/gbatek.htm) for ds technical info
* Thanks to Gericom for the original C# Header and ARM9 decompression code from EFE(Every File Explorer), as well as for testing.

# Others
For reference and tutorials about Ghidra loaders and how to write them, you may want to read:
* [Tutorial: Writing a Ghidra loader](https://pedro-javierf.github.io/devblog/tutorialwritingaghidraloader/)
* [Advanced Ghidra Loader: labels, overlays and Function ID](https://pedro-javierf.github.io/devblog/advancedghidraloader/)
