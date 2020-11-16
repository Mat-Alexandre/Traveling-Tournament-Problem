package src;

import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class OpenFile{

    public static int[][] getFile(String pathName){
        int[][] table = null;
        String currDir = System.getProperty("user.dir");
        
        if(pathName == null)
        {
            pathName = "\\datasets\\dataset1";
        }

        try{
            Scanner sc = new Scanner(new File(currDir + pathName));
            // Get the size of table in file
            int tableSize = sc.nextInt();
            // Skip current line
            sc.nextLine();
            table = new int[tableSize][tableSize];
            // Get all integers in file
            while(sc.hasNext()){
                for(int i = 0; i < tableSize; i++){
                    for(int j = 0; j < tableSize; j++){
                        table[i][j] = sc.nextInt();
                    }
                }
            }
            sc.close();
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }

        return table;
    }
}