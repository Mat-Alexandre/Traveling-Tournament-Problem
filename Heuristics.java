import java.util.Random;

public class Heuristics {
    
    /**
     *  Representação do problema:
     *      Para representar cada solução será utilizado uma matriz [n, 2(n-1)] onde n é a 
     * quantidade de times.
     *      As linhas da matriz serão os times enquanto as colunas representarão as rodadas
     * dos jogos. Cada elemento da matriz deverá conter um inteiro referente à um time, e 
     * o sinal do número indicará se o time jogará dentro ou fora de casa.
     * Ex.: s[1, 2] = -3
     *      Indica que o time 1 na rodada 2 jogará contra o time 3 fora de casa.
     * 
     */
    
    public static final int SEM_TIME = 0;

    public static void SimulatedAnnealing(int[][] table, int[][] s){
        // CALCULAFO NÃO REPRESENTA NADA NO MOMENTO ATUAL
        // Gerar a solução inicial
        // Temperatura Inicial
        do{
            do{
                // Gerar um vizinho aleatório
                // deltaE = Diferença entre f(s') e f(s)
                if(calculaFO(table, s) == 1/* deltaE <= 0 */)
                {
                    calculaFO(table, s);
                    // Aceita o vizinho como solução
                }else
                {
                    calculaFO(table, s);
                    // s' será aceito com uma probabilidade e^(-deltaE / T)
                }
            }while(false/* Condição de Equilíbrio */); // Ex.: um dado número de iterações executado em cada temperatura
            // Atualiza a temperatura
        }while(false/* Critério de parada */); // Ex.: T < Tmin

        // return s (melhor solução)
    }

    public static int[][] geraSolucaoInicial(int[][] table) throws NullPointerException
    {
        int [][] s = new int[table.length+1][2 *(table.length - 1)];

        // Construção da solução inicial através de backtracking
        if(solucaoBacktracking(s, 1, 0) == false)
            return null;
        return s;
    }

    private static boolean solucaoBacktracking(int[][] solucao, int time, int rodada)
    {
        if(rodada == (solucao[0].length / 2))
        {
            if(time == solucao.length-1)
                return true;
            else if(solucaoBacktracking(solucao, time + 1, 0))
                return true;
            else
                return false;
        }
        
        // Para cada time
        // Verificar se o time' pode ser colocado na rodada
        int fora_seq = 0;
        for(int adversario = 1; adversario < solucao.length; adversario++)
        {
            // Se respeita as restrições
            if(solucao[time][rodada] == SEM_TIME && rodada < (solucao[0].length / 2))
            {
                if(respeitaRestricoes(solucao, time, adversario, rodada))
                {
                    // Posição válida
                    // Preenchendo as 2(n - 1) rodadas de forma espelhada
                    int m_round = (solucao[0].length / 2);
                    int mult = 0;
                    if( (new Random()).nextInt(1) == 1 && fora_seq < 3)
                    {
                        fora_seq++;
                        mult = -1;
                    }else
                    {
                        fora_seq = 0;
                        mult = 1;
                    }

                    solucao[time][rodada] = mult * adversario;
                    solucao[adversario][rodada] = -mult * time;
                    solucao[time][rodada + m_round] = -mult * adversario;
                    solucao[adversario][rodada + m_round] = mult * time;
                    // Backtracking para a próxima rodada
                    if(solucaoBacktracking(solucao, time, rodada + 1))
                        return true;
                        
                    // Desfazer alteração
                    solucao[time][rodada] = SEM_TIME;
                    solucao[adversario][rodada] = SEM_TIME;
                    solucao[time][rodada + m_round] = SEM_TIME;
                    solucao[adversario][rodada + m_round] = SEM_TIME;
                }
            }
            else
            {
                rodada++;
            }
        }

        return true;
    }

    private static boolean respeitaRestricoes(int[][] s, int time, int adversario, int rodada)
    {
        // Time é o próprio adversário
        if(time == adversario)
            return false;

        // Adversario já está jogando na rodada atual
        if(s[adversario][rodada] != SEM_TIME)
            return false;
        
        // Time já jogou na rodada atual
        if(s[time][rodada] != SEM_TIME)
            return false;
        
        // Time já jogou contra adversário
        for(int rodadas = 0; rodadas < s[0].length / 2; rodadas++)
        {
            if(s[time][rodadas] == adversario)
                return false;
        }

        return true;
    }

    public static double calculaFO(int[][] table, int[][] s)
    {
        double fo = 0, penalidade = 0;
        
        // Calculo da penalidade
        for(int time = 0; time < table.length - 1; time++)
            for(int adv = time + 1; adv < table[0].length; adv++)
                penalidade += table[time][adv];
        
        // Calculo da FO
        for(int time = 1; time < s.length; time++)
        {
            int fora_seq = 0, dentro_seq = 0;
            int t, a;
            for(int rodada = 0; rodada < s[0].length; rodada++)
            {
                // Verificando se o time jogou mais de 3x
                // dentro ou fora de casa até a rodada atual
                if(s[time][rodada] < 0)
                {
                    dentro_seq = 0;
                    fora_seq++;
                }
                else{
                    fora_seq = 0;
                    dentro_seq++;
                }
                
                // Adicionar penalidade se time jogou
                // mais de 3x dentro ou fora de casa
                if(fora_seq > 3 || dentro_seq > 3 )
                    fo += penalidade;
                
                // Cálculo das distâncias dij para cada time
                if(rodada == 0)
                {
                    t = Math.abs(time-1);
                    a = Math.abs(s[time][0])-1;
                }
                else /*if(rodada <= s[0].length)*/
                {
                    t = Math.abs(s[time][rodada-1])-1;
                    a = Math.abs(s[time][rodada])-1;
                }
                fo += table[t][a];
            }
            // Adicionando a volta do time para casa
            t = Math.abs(s[time][s[0].length-1])-1;
            a = Math.abs(time-1);
            fo += table[t][a];
        }
        return fo;
    }

    public static int[][] geraVizinho(int[] table, int[][] s)
    {
        int [][] s_ = s;
        switch(1){
            case 1:
                s_ = trocaCasa(s);
                break;
            
            case 2:
                s_ = trocaRodada(s);
                break;
            
            case 3:
                s_ = trocaTime(s);
                break;
            
            case 4:
                s_ = trocaParcialTime(s);
                break;
            
            default:
                s_ = s;
                break;
        }
        return s_;
    }

    private static int[][] trocaCasa(int[][] s)
    {
        return s;
    }

    private static int[][] trocaRodada(int[][] s)
    {
        return s;
    }

    private static int[][] trocaTime(int[][] s)
    {
        return s;
    }

    private static int[][] trocaParcialTime(int[][] s)
    {
        return s;
    }
}
