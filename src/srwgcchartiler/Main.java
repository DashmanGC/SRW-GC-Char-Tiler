/*
 * Copyright (C) 2014 Dashman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package srwgcchartiler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonatan
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length == 0)
            processBMPfiles();
        else{
            if (args.length != 1){
                showErrorMsg();
                return;
            }

            injectCharacters(args[0]);
        }

    }

    public static void showErrorMsg(){
        System.err.println("ERROR: Wrong number of parameters.");
        System.err.println("USAGE:\n" +
                "a) java -jar char_tiler.jar\n" +
                " * This processes all 18x18 BMP files in the program's folder.\n" +
                "b) java -jar char_tiler.jar <font_file>\n" +
                " * This inserts the contents of the processed BIN files inside the font_file at the position indicated" +
                " by their filenames (must be a number, like 012.BIN)");
    }

    public static void processBMPfiles(){
        // Find all BMP files in the same folder as the program
        File folder = new File(".");
        System.out.println(folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".BMP") || (
                        filename.endsWith(".bmp")
                        )); }
        });

        /*
         * The problem with the BMP files are:
         * 1) Image data is stored upside down
         * 2) Each row of image data has padding to make the row a multiple of 4 (in our case,
                rows of 18 4bpp pixels should be 9 bytes, but are stored instead as 12 bytes)
         *
         * Additionally, the font graphics use reversed nibbles, so we have to flip every byte in the end.
         */

        byte[] tile = new byte[162];    // The real size of the image data, without padding
        byte[] aux = new byte[216];     // The image data expected in the bmp file (with padding)
        byte[] check = new byte[1];     // Used to check the width and height
        int counter = 0;

        // For each file found, if height and size is 18
        for (int index = 0; index < listOfFiles.length; index++){
            try {
                RandomAccessFile f = new RandomAccessFile(listOfFiles[index].getAbsolutePath(), "r");
                f.seek(18);
                f.read(check);
                if (check[0] != 18) // Check the width
                    continue;

                f.seek(22);
                f.read(check);
                if (check[0] != 18) // Check the height
                    continue;

                f.seek(f.length() - 216);
                f.read(aux);

                f.close();

                // From the last 216 bytes of the file, grab the first 9 bytes of each 12 bytes
                // and store them in the byte array
                for (int i = 0; i < 18; i++){   // There's 18 rows of 12 bytes
                    tile[i*9] = aux[i*12];
                    tile[i*9 + 1] = aux[i*12 + 1];
                    tile[i*9 + 2] = aux[i*12 + 2];
                    tile[i*9 + 3] = aux[i*12 + 3];
                    tile[i*9 + 4] = aux[i*12 + 4];
                    tile[i*9 + 5] = aux[i*12 + 5];
                    tile[i*9 + 6] = aux[i*12 + 6];
                    tile[i*9 + 7] = aux[i*12 + 7];
                    tile[i*9 + 8] = aux[i*12 + 8];
                }


                // Turn the byte array upside-down
                byte[] tile_R = tile.clone();
                int dimX = 9;
                for (int i = 0, j = tile.length - dimX; i < tile.length; i += dimX, j -= dimX) {
                    for (int k = 0; k < dimX; ++k) {
                        //System.out.println("Length: " + pixels.length + " i: " + i + " j: " + j + " k: " + k);
                        tile[i + k] = tile_R[j + k];
                    }
                }

                // Switch all nibbles in the tile
                for (int i = 0; i < tile.length; i++){
                    int lo = tile[i] & 0x0f;
                    int hi = tile[i] & 0xf0;

                    tile[i] = (byte) (lo << 4 | hi >> 4);
                }

                // Output the byte array to a file with the same name of the original, but with extension ".bin"
                String name = listOfFiles[index].getName() + ".bin";

                f = new RandomAccessFile(name, "rw");

                f.write(tile);

                f.close();

                System.out.println("File " + name + " created successfully.");
                counter++;

            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Finished. " + counter + " files processed.");
        // END
    }

    public static void injectCharacters(String font_file){
        // Make sure the font file exists
        File check = new File(font_file);
        if (!check.exists()){
            System.err.println("ERROR: Font file not found!!");
            return;
        }

        // Find all BIN files in the folder
        File folder = new File(".");
        System.out.println(folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".BIN") || (
                        filename.endsWith(".bin")
                        )); }
        });

        int counter = 0;
        byte[] tile = new byte[162];

        // For each BIN file
        for (int i = 0; i < listOfFiles.length; i++){
            String name = listOfFiles[i].getName();

            int name_number;

            // Make sure the first three characters form a number
            try{
                name_number = Integer.parseInt(name.substring(0, 3));

                // Make sure the file size is 162
                try{
                    RandomAccessFile f = new RandomAccessFile(name, "r");

                    if (f.length() == 162){
                        f.read(tile);

                        f.close();

                        // Insert the contents of the BIN file at the position indicated by the file name
                        // The start position would be (164 * name_number) + 2
                        f = new RandomAccessFile(font_file, "rw");

                        int offset = (164 * name_number) + 2;

                        f.seek(offset);

                        f.write(tile);

                        f.close();

                        counter++;
                    }
                    else{
                        System.err.println("WARNING: File " + name + " is not 162 bytes long. The file will be ignored.");
                    }
                }
                catch (IOException ex2){
                    System.err.println("ERROR: Problem reading BIN or FONT file.");
                }
            }
            catch(NumberFormatException ex){
                System.err.println("WARNING: File " + name + " doesn't start with a number. The file will be ignored.");
            }
        }

        System.out.println("Finished. " + counter + " characters inserted.");
        // END
    }
}
