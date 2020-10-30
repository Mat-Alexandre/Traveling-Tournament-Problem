import java.util.Arrays;
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

    public static int[][] SimulatedAnnealing(int[][] table, float temp_inicial, float temp_final, int max_ite, double alfa) throws NullPointerException{
        // CALCULAFO NÃO REPRESENTA NADA NO MOMENTO ATUAL
        // Gerar a solução inicial
        // Temperatura Inicial
        float temperatura = temp_inicial;
        int[][] s = geraSolucaoInicial(table);
        int[][] viz = null;
        double fo_s, fo_viz;
        
        do{
            int i = 0;
            do{
                // Gerar um vizinho aleatório
                viz = geraVizinho(table, s);
                // deltaE = Diferença entre f(s') e f(s)
                fo_s = calculaFO(table, s);
                fo_viz = calculaFO(table, viz);
                double deltaE = fo_viz - fo_s;

                if(deltaE <= 0/* deltaE <= 0 */)
                {
                    // Aceita o vizinho como solução
                    s = viz;
                }else
                {
                    calculaFO(table, s);
                    // s' será aceito com uma probabilidade e^(-deltaE / T)
                    s = ((new Random()).nextDouble() <= Math.exp( (-deltaE)/temperatura ))? viz : s;
                }
            }while(i < max_ite); // Ex.: um dado número de iterações executado em cada temperatura
            // Atualiza a temperatura
            temperatura *= alfa;
        }while(temperatura < temp_final); // Ex.: T < Tmin

        // return s (melhor solução)
        return s;
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

    private static int[][] geraVizinho(int[][] table, int[][] s)
    {
        int [][] s_ = null;
        switch((new Random()).nextInt(3) + 1){
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
        // Seleciona aleatoriamente 2 times para 
        // trocar quem joga dentro/fora de casa
        int time, adversario, rodada;
        Random rng = new Random();

        time = 1 + rng.nextInt(s.length - 1);
        rodada = rng.nextInt(s[0].length/2);

        adversario = Math.abs(s[time][rodada]);

        // Trocando o mando de campo de T e A
        for(int rodadas = 0; rodadas < s[0].length; rodadas++)
        {
            if(s[time][rodadas] == -adversario)
            {
                // Troca
                s[time][rodadas] *= -1;
                s[adversario][rodadas] *= -1;

                s[time][rodada] *= -1;
                s[adversario][rodada] *= -1;
                break;
            }
        }

        return s;
    }

    private static int[][] trocaRodada(int[][] s)
    {
        // Troca duas rodadas randomicamente
        int r1, r2;
        Random rng = new Random();
        
        r1 = rng.nextInt(s[0].length);
        r2 = rng.nextInt(s[0].length);

        // Trocando as rodadas r1 e r2
        for(int times = 1; times < s.length; times++)
        {
            int aux = s[times][r1];
            s[times][r1] = s[times][r2];
            s[times][r2] = aux;
        }
        return s;
    }

    private static int[][] trocaTime(int[][] s)
    {
        // Troca a lista de times que T jogaria
        // com a lista de A
        int time, adversario;
        Random rng = new Random();

        time = 1 + rng.nextInt(s.length - 1);
        adversario = time;
        
        // Certificando que os dois times são diferentes
        while(time == adversario) adversario = 1 + rng.nextInt(s.length - 1);
        
        for(int rodada = 0; rodada < s[0].length; rodada++)
        {
            if(s[time][rodada] == adversario) continue;
            else
            {
                int aux = s[time][rodada];
                s[time][rodada] = s[adversario][rodada];
                s[adversario][rodada] = aux;
            }
        }

        return s;
    }

    private static int[][] trocaParcialTime(int[][] s)
    {
        // @ATENÇÃO:
        // FUNTIONANDO MAS 341,342,352,353,372,373 PRECISAM SER ALTERADAS PARA RESPEITARAM MANDO DE CASA
        // Seleciona aleatoriamente uma rodada e 2 times
        // Troca os adversários de cada time naquela rodada
        // e altera a tablea 
        int[] times = new int[2];
        int rodada;
        Random rng = new Random();

        times[0] = 1 + rng.nextInt(s.length - 1);
        times[1] = times[0];

        while(times[0] == times[1] ) times[1] = 1 + rng.nextInt(s.length - 1);

        rodada = rng.nextInt(s[0].length);

        while( s[ times[0] ][rodada] == times[1] ) rodada = rng.nextInt(s[0].length);

        // DEBUG
        times[0] = 2;
        times[1] = 4;
        rodada = 8; // = 9

        // troca os adversários de 2 e 4 na rodada 9
        int aux = s[ times[0] ][rodada];
        s[ times[0] ][rodada] = s[ times[1] ][rodada];
        s[ times[1] ][rodada] = aux;
        
        // e arruma a rodada
        s[ Math.abs(s[ times[0] ][rodada]) ][rodada] = times[0];
        s[ Math.abs(s[ times[1] ][rodada]) ][rodada] = times[1];

        // rodadas em que 2 e 4 jogavam contra seus atuaais adv. na rodada 9
        int rodada_ = 0;
        for(int i = 0; i < s[0].length; i++)
        {
            if( i != rodada && s[ times[0] ][i] == s[ times[0] ][rodada] )
            {
                // troca os jogos de 2 e 4 na rodada i
                aux = s[ times[0] ][i];
                s[ times[0] ][i] = s[ times[1] ][i];
                s[ times[1] ][i] = aux;

                // Arruma a rodada i
                s[ Math.abs(s[ times[0] ][i]) ][i] = times[0];
                s[ Math.abs(s[ times[1] ][i]) ][i] = times[1];
            }
            if( i != rodada && s[ times[1] ][i] == s[ times[1] ][rodada] )
            {
                // troca os jogos de 2 e 4 na rodada i
                aux = s[ times[0] ][i];
                s[ times[0] ][i] = s[ times[1] ][i];
                s[ times[1] ][i] = aux;

                // Arruma a rodada i
                s[ Math.abs(s[ times[0] ][i]) ][i] = times[0];
                s[ Math.abs(s[ times[1] ][i]) ][i] = times[1];
                
                rodada_ = i;
            }
        }

        // Aletrando a rodada em que o time 4 joga contra seu atual adv.
        // na rodada I
        
        for(int i = 0; i < s[0].length; i++)
        {
            if( i != rodada_ && s[ times[1] ][i] == s[ times[1] ][rodada_] )
            {
                // troca os jogos de 2 e 4 na rodada i
                aux = s[ times[0] ][i];
                s[ times[0] ][i] = s[ times[1] ][i];
                s[ times[1] ][i] = aux;

                // Arruma a rodada i
                s[ Math.abs(s[ times[0] ][i]) ][i] = times[0];
                s[ Math.abs(s[ times[1] ][i]) ][i] = times[1];
                break;
            }
        }
        System.out.println("------------");

        return s;
    }
}
