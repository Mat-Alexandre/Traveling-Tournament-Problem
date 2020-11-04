import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class OpenFile{

    public static int[][] getFile(){
        int[][] table = null;
        try{
            Scanner sc = new Scanner(new File("dataset2"));
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