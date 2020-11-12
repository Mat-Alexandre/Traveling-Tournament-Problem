package src;

public class Main {
    /**
     * Imprime a tabela de solução formatada
     * 
     * @param table solução a ser impressa
     */
    public static void printTable(int[][] table){
        for(int i = 1; i < table.length; i++){
            System.out.printf("%2d: ", i);
            for(int j = 0; j < table[0].length; j++){
                System.out.printf("%2d ", table[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    
    public static void main(String[] args) {
        int[][] table = OpenFile.getFile();
        int[][] s;
        
        try{
            
            s = Heuristics.SimulatedAnnealing(table, 0.01f, 0.97);
            
            System.out.println("Solução final:");
            printTable(s);

            System.out.println("FO(s): " + Heuristics.calculaFO(table, s));

        }catch(NullPointerException npe){
            System.err.println("Não foi possível construir uma solução inicial.");
        }
    }
}
