public class Main {
    public static void printTable(int[][] table){
        System.out.println();
        for(int i = 1; i < table.length; i++){
            System.out.printf("%2d: ", i);
            for(int j = 0; j < table[0].length; j++){
                System.out.printf("%2d ", table[i][j]);
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
        int[][] table = OpenFile.getFile();
        
        int[][] s = {
            {0,0,0,0,0,0,0,0,0,0},
            {6,-2,4,3,-5,-4,-3,5,2,-6},
            {5,1,-3,-6,4,3,6,-4,-1,-5},
            {-4,5,2,-1,6,-2,1,-6,-5,4},
            {3,6,-1,-5,-2,1,5,2,-6,-3},
            {-2,-3,6,4,1,-6,-4,-1,3,2},
            {-1,-4,-5,2,-3,5,-2,3,4,1}
        };
        
        // int [][]s = null;
        try{
            // s = Heuristics.geraSolucaoInicial(table);
            System.out.println("Result Table:");
            printTable(s);
            // s = Heuristics.SimulatedAnnealing(table, 10, 0.01f, (s.length - 1)*((s.length - 1)-1)/2, 0.97);
            s = Heuristics.trocaParcialTime(s);
            printTable(s);
        }catch(NullPointerException npe){
            System.err.println("Não foi possível construir uma solução inicial.");
        }
    }
}
