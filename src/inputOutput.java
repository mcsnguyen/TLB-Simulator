import javax.xml.crypto.OctetStreamData;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Iterator;


public class inputOutput {

    //Declare size of TLB, RAM, Page table
    int TLBSize = 16;
    int RAMSize;
    int pageTableSize = 16;

    //Initialize the MMU Data and OS Sizes.

        //TLBSize = 16, pageTableSize = 16
        MMU MMUData = new MMU(TLBSize,pageTableSize);

        //memorySize = 16, memoryDataSize = 2^12 (12 bit width) = 4096, PageDataSize = 16
        OS OSData = new OS(16,4096,16);

    private int counter = 1;
    StringBuilder sb = new StringBuilder();
    private int addressValue;

    int dirtyBitCounter=0;


    @SuppressWarnings("Duplicates")
    public void readFile() throws IOException{

        //Checks if directory exsist, if not create it.
        String folderPath = "src/overwriteFiles/overwritten page files";
        File outputFolder = new File(folderPath);
        if(!outputFolder.exists()){
            outputFolder.mkdir();
        }


        //Create a for-loop to read all testfiles
        for (int fileCount = 1; fileCount<5;fileCount++) {

            try {

                //Counts the dirtyBit
                dirtyBitCounter++;

                //ReadFile
                File testdata1 = new File("src/originalFiles/test_files/test_" + fileCount + ".txt");
                BufferedReader reader1 = new BufferedReader(new FileReader(testdata1));

                //CSV File Header that accepts the fileCount number. ex: testfile(1)
                writeCSVFileHeader(fileCount);

                Scanner scan = new Scanner(reader1);

                int readOrWrite;
                String pageNumAndOffset;

                //Begin reading testdata1
                while (scan.hasNext()) {

                    readOrWrite = scan.nextInt();

                    //READ
                    if (readOrWrite == 0) {

                        counter++;
                        //System.out.println("its READ: " + readOrWrite);

                        pageNumAndOffset = scan.next();

                        int mid = pageNumAndOffset.length() / 2;

                        String pageAddress = pageNumAndOffset.substring(0, mid);
                        String addressLocation = pageNumAndOffset.substring(mid);

                        //pageAddress and Offset from HEX > Decimal (int) format.
                        int pageAddressInt = Integer.parseInt(pageAddress, 16);
                        int offsetInt = Integer.parseInt(addressLocation, 16);
                        //System.out.print(Integer.toBinaryString(offsetInt));

                            /*Printing out Read Values in console.
                            System.out.println("  read address: " + pageNumAndOffset);
                            System.out.println("    -page address: " + pageAddress);
                            System.out.println("    -address location: " + addressLocation);
                                    //System.out.println("    -HEX to Binary: " +offsetInt);
                            */

                        //Goes into the FindPageAddressAndOffset method to find the
                        //address page and the offset address value
                        FindPageAddressAndOffset(pageAddress, offsetInt);

                        //MMUData.executeInstruction(readOrWrite, pageAddressInt,offsetInt,0);

                        //OSData.addToDisk(pageAddressInt,offsetInt,addressLocation);

                        //Writes the Body for the CSV Files (REMOVE THIS TO WRITE FILE)
                        writeCSVFileBody(pageAddress, readOrWrite, addressValue, 0, 0, 0, 0, 0);
                    }

                    //WRITE
                    else if (readOrWrite == 1) {

                        counter++;
                        //System.out.println("ITS WRITE: " +object1);

                        pageNumAndOffset = scan.next();
                        //System.out.println("    Reading and will write: " +pageNumAndOffset);

                        int mid = pageNumAndOffset.length() / 2;

                        String pageAddress = pageNumAndOffset.substring(0, mid);
                        String addressLocation = pageNumAndOffset.substring(mid);

                        int pageAddressInt = Integer.parseInt(pageAddress, 16);
                        int offsetInt = Integer.parseInt(addressLocation, 16);

                        int overWriteAddress;
                        overWriteAddress = scan.nextInt();

                            /* Displaying Write information into console.
                            System.out.println("  write address: " +pageNumAndOffset);
                            System.out.println("    -page address: " +pageAddress);
                            System.out.println("    -address location: " +addressLocation);
                            System.out.println("    -HEX to Binary: " +offsetInt);
                            System.out.println("    -writing Value: " +overWriteAddress);
                            */

                        FindPageAddressAndOffset(pageAddress, offsetInt, overWriteAddress);
                        //int[] csvData = MMUData.executeInstruction(readOrWrite, pageAddressInt,offsetInt, overWriteAddress);


                        //REMOVE THIS TO WRITE FILE //csvData[1], csvData[2], csvData[3], csvData[4], csvData[5]
                        writeCSVFileBody(pageAddress, readOrWrite, addressValue, 0,0,0,0,0);
                    }

                    //System.out.println("---------------------------------------------------------");

                }

                scan.close();
                System.out.println("File " + fileCount+" read/write complete.");


            } catch (IOException e) {

                System.out.println("Error in the inputOutput --> readFile");
                e.printStackTrace();

            }

        }

        System.out.println("All files have been read/write. check file directory for outputs.");

    }

    //1. Read: Will go to the pageAddress and find its offsetLocation, then spit out the value in the offset.
    @SuppressWarnings("Duplicates")
    public void FindPageAddressAndOffset(String pageFileName, int offsetLocation) {  //String PageFileName

        int[] offsetData = new int[256];

        try {

            //Check if File exist.
            File checkFile = new File("src/originalFiles/page_files/" + pageFileName + ".pg");
            BufferedReader buffReader = new BufferedReader(new FileReader(checkFile));

            if(checkFile.exists()) {

                //System.out.println("    ^yeap the file is here!");
            }
            else if (checkFile.exists() == false) {

                System.out.println("    (X) the file doesnt exist!");
            }

            Scanner scan = new Scanner(buffReader);

            int i=0;
            while (scan.hasNext()) {

                int line = scan.nextInt();
                offsetData[i] = line;

                i++;

            }

            int offsetplus1 = offsetLocation + 1;

            //System.out.println("    (=)at address: " +offsetplus1+", value is: " +offsetData[offsetLocation]);

            //Returns the pageLocation --> address value
            returnAddressValue(offsetData[offsetLocation]);


        }
        catch(IOException e){

            System.out.println("Error in the inputOutput/FindPageAddressAndOffsetMethod");
            e.printStackTrace();

        }

    }

    //2. Overwrite File (Page File Location, Page Address within the file, the value to replace in the address in page.
    @SuppressWarnings("Duplicates")
    public void FindPageAddressAndOffset(String pageFileName, int offsetLocation, int overwriteAddress){

        //.PG file entries size (given)
        int[] offsetData = new int[256];

        try {

            //Declaring filereader
            File checkFile = new File("src/originalFiles/page_files/" + pageFileName + ".pg");//src/originalFiles/page_files/

            //Overwrite File for Read
            File overwriteFile = new File("src/overwriteFiles/overwritten page files/" + pageFileName + ".pg");//src/overwriteFiles/write txt/

            //Check if overwrite file exisit.
            boolean exist = overwriteFile.exists();

            //System.out.println("Does overwrite exist?: " +exist);


            //Check if File exist. if overwrite.pg exsist, use overwrite. if not use original
            if (overwriteFile.exists() == true) {

                //System.out.println("    (W)overwrite file exist. changing checkFile to OverWriteFile");
                checkFile = new File("src/overwriteFiles/overwritten page files/" + pageFileName + ".pg");
                //checkFile.createNewFile();

            }

            BufferedReader buffReader = new BufferedReader(new FileReader(checkFile));

            //Scanner with buffreader
            Scanner scan = new Scanner(buffReader);
            int counter=0;

            //Reads File and stores it into a array
            while (scan.hasNext()) {

                int line = scan.nextInt();
                offsetData[counter] = line;

                counter++;

            }

            int temp = offsetData[offsetLocation];

            buffReader.close();

            //Overwrites the offsetlocation to the new address
            offsetData[offsetLocation] = overwriteAddress;

            //Writes and Overwrites the address
            FileWriter writer = new FileWriter("src/overwriteFiles/overwritten page files/" + pageFileName + ".pg");
            BufferedWriter bufferedWriter= new BufferedWriter(writer);

            //Writes File (dont write yet)
            for (int i=0; i<offsetData.length; i++){

                //System.out.println(offsetData[i]);
                bufferedWriter.write(offsetData[i]+""+"\n");

            }
            bufferedWriter.flush();
            bufferedWriter.close();

            int offsetplus1 = offsetLocation + 1;

            //System.out.println("    (=)at address: " +offsetplus1+", old value is: " +temp);
            //System.out.println("    (!)at address: " +offsetplus1+", new value is: " +offsetData[offsetLocation]);

            returnAddressValue(offsetData[offsetLocation]);

        }
        catch(IOException e){

            System.out.println("Error in the inputOutput/FindPageAddressAndOffsetMethod(write)");
            e.printStackTrace();

        }

    }

    //CSVFile Write Header
    //Parameters must be set so that it gets: (page_address, read/write, read/write value (from file and to file)
    public void writeCSVFileHeader(int fileNumber) throws IOException{

        String comaDelimiter = ",";
        String newLineSeparator = "\n";

        //Checks if directory exist, if not create it.
        String folderPath = "src/CSVFiles";
        File outputFolder = new File(folderPath);

        if(!outputFolder.exists()){
            outputFolder.mkdir();
        }

        //Filepath Location
        File writeFile = new File("src/CSVFiles/testfile"+fileNumber+".csv");

        try{

            FileWriter filewriter = new FileWriter(writeFile);
            String entries = ("Address, Read/Write, Value, Soft,Hard,Hit,Evicted_pg#,Dirty_evicted_page");

            sb.append(entries);
            sb.append(comaDelimiter);
            sb.append("\n");

            filewriter.write(sb.toString());

        }
        catch (IOException e){
            System.out.println("Error: Write to CSV File Failed. Check method");
            e.printStackTrace();
        }

    }

    /*CSFFile Write Body
        -Accepts the parameters and writes to the file according to the given project format.
    */
    @SuppressWarnings("Duplicates")
    public void writeCSVFileBody(String pageLocation, int readOrWrite, int addressValue,
                                          int softValue, int hardValue, int hitValue,
                                          int evictedPG, int dirtyPage){

        String comaDelimiter = ",";
        String newLineSeparator = "\n";

        File writeFile = new File("src/CSVFiles/testfile1.csv");

        try {

            FileWriter filewriter = new FileWriter(writeFile);
            sb.append(pageLocation);
            sb.append(comaDelimiter);
            sb.append(readOrWrite + "\t");
            sb.append(comaDelimiter);
            sb.append(addressValue + "\t");
            sb.append(comaDelimiter);
            sb.append(softValue + "\t");
            sb.append(comaDelimiter);
            sb.append(hardValue + "\t");
            sb.append(comaDelimiter);
            sb.append(hitValue + "\t");
            sb.append(comaDelimiter);
            sb.append(evictedPG + "\t");
            sb.append(comaDelimiter);
            sb.append(dirtyPage + "\t");
            sb.append("\n");

            filewriter.write(sb.toString());

            filewriter.flush();
            filewriter.close();

            //System.out.println("write file sucessful");
        }
        catch(IOException e){
            System.out.println("Error in the WriteBody method");
            e.printStackTrace();
        }

    }

    //Returns the offset value in the pageaddress.
    public int returnAddressValue(int temp){

        return addressValue = temp;

    }


}
