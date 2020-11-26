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
                System.out.printf("%3d ", table[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[][] s;
        int[][] table;
        String pathName;

        if( args.length == 0 )
        {
            System.out.println("Sem arquivo especificado. Instância automática.");
            pathName = null;
        }else
        {
            pathName = args[0];
            System.out.println("Caminho selecionado: " + pathName);
        }
        
        try{
            table = OpenFile.getFile(pathName);
            s = Heuristics.TTSA(table, 400, 0.9999f, 4000, 1.04f, 1.04f, 5000, 7100, 10);
            
            System.out.println("Solução final:");
            printTable(s);

            int vio_s = Heuristics.violacoes(s);
            System.out.println("Custo da solução: " + Heuristics.custo(table, s, vio_s, 4000f));
            System.out.println("Número de violações: " + vio_s);

        }catch(NullPointerException npe){
            System.err.println("Não foi possível construir uma solução inicial.");
        }
    }
}
