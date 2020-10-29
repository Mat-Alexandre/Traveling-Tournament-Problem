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
        int[][] s;
        
        try{
            s = Heuristics.geraSolucaoInicial(table);
            System.out.println("Result Table:");
            printTable(s);
            double fo = Heuristics.calculaFO(table, s);
            System.out.println(fo);
        }catch(NullPointerException npe){
            System.err.println("Não foi possível construir uma solução inicial.");
        }
    }
}
