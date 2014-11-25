SRW GC Char Tiler by Dashman

What does this do?
--------------------

This program inserts images as characters in one of the font files that the game Super Robot Wars GC uses.

This takes a couple of steps:

1) Create BMP files of each character you want to insert (replace) in the font. Each of these BMP files has to be 18x18 pixels with indexed colours and 4bpp (16 colours). Only the first 5 colours are used, and colour 0 is the transparency.

2) Place the program (char_tiler.jar) in the same folder as all the BMP files. Double-click on it. This will create a BIN file for each BMP file it finds (with dimensions 18x18). If you want to do this on the command line, just execute:

java -jar char_tiler.jar

3) Rename the generated BIN files. The only important thing is that their 3 first characters form a 3 digit number (like 012 or 064) corresponding to the position of the character in the font file. A way to know which character is what position is opening the font file in CrystalTile2. More instructions on this at the end.

4) Open a command / shell window and browse to the folder where the BIN files and the executable are. Execute:

java -jar char_tiler.jar <font_file>

where font_file is the font file where you want to insert (replace) the characters (for example, Ft01.font). The font file MUST be present in the folder with the other BIN files. The font file is overwritten, so keep some backups somewhere.


How to view a font file in CrystalTile2
----------------------------------------

Open CrystalTile2. Drag and drop the font file on it (the left part of the UI, if you get a pop-up window, you dropped it in the wrong place).

Now in the upper menu go to "View -> Tile Viewer" (or press F5). Also check "View -> Fit Window to Tile".

In the left menu, on the Properties tab, use the following settings in Tile property:

- scale: 200
- byte jump: 2
- width: 18
- height: 18
- Tile form: GBA 4bpp

The colours will be wrong. You can play with the colours in the Palette tab to make it easier for you to distinguish characters.

Now, go to "View -> Tile Line Numbers". Every tile will be numbered now. These numbers are the ones you have to use in the BIN files.



