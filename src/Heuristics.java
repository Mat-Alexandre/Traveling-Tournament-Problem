package src;

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

    /**
     * Heurística para busca de ótimos locais de uma solução
     * através de um paralelo feito com fabricação de ligas metálicas
     * 
     * @param table      tabela de distância
     * @param T_ini      temperatura inicial
     * @param beta       multiplicador da temperatura
     * @param peso       punição de violação 
     * @param delta      multiplicador do peso caso "s" seja inviável
     * @param teta       divisor do peso caso "s" seja viável
     * @param max_count  número máximo de iterações para explorar um espaço viável
     * @param max_ite    número máximo de iterações para cada espaço de soluções
     * @param max_reheat número máximo de reaquecimentos
     * @return solução com o melhor custo, respeitando ou não as restrições
     * @throws NullPointerException
     */
    public static int[][] TTSA(int[][] table, float T_ini, float beta, float peso, float delta, float teta, int max_count, int max_ite, int max_reheat) throws NullPointerException
    {
        int[][] s = geraSolucaoInicial(table), viz = null;
        double bestFeasible   = Double.MAX_VALUE, 
               nbf            = Double.MAX_VALUE,
               bestInfeasible = Double.MAX_VALUE, 
               nbi            = Double.MAX_VALUE,
               deltaC;

        float temperatura = T_ini,
        bestTemperature = temperatura;

        int reheat = 0, counter = 0, ite, vio_s, vio_viz;
        boolean aceita;

        while( reheat <= max_reheat )
        {
            ite = 0;

            while( ite <= max_ite )
            {

                while( counter < max_count )
                {
                    viz = geraVizinho(table, copiaSolucao(s));
                    vio_s = violacoes(s);
                    vio_viz = violacoes(viz);
                    
                    double c_viz = custo(table, viz, vio_viz, peso),
                    c_s = custo(table, s, vio_s, peso);
    
                    if(c_viz < c_s || vio_viz == 0 && c_viz < bestFeasible || vio_s > 0 && c_viz < bestInfeasible)
                    {
                        aceita = true;
                    }else 
                    {
                        deltaC = c_viz - c_s;
                        if( (new Random()).nextDouble() <= Math.exp( (-deltaC) / temperatura ))
                        {
                            aceita = true;
                        }else
                        {
                            aceita = false;
                        }
                    }
    
                    if(aceita)
                    {
                        s = viz;
                        vio_s = violacoes(s);
                        c_s = custo(table, s, vio_s, peso);
                        if(vio_s == 0)
                        {
                            nbf = Double.min(c_s, bestFeasible);
                        }else
                        {
                            nbi = Double.min(c_s, bestInfeasible);
                        }
    
                        if(nbf < bestFeasible || nbi < bestInfeasible)
                        {
                            reheat = 0; counter = 0; ite = 0;
                            bestTemperature = temperatura;
                            bestFeasible = nbf;
                            bestInfeasible = nbi;
    
                            if(vio_s == 0)
                            {
                                peso = (float)(peso / teta);
                            }else
                            {
                                peso = (float)(peso * delta);
                            }
                        }else
                        {
                            counter++;
                        }
                    }
                }
                ite++;
                temperatura = (float)(temperatura * beta);
            }
            reheat++;
            temperatura = (float)(2*bestTemperature);
        }

        return s;
    }
    
    /**
     * Gera uma solução inicial via backtracking
     * 
     * @param table tabela de distância
     * @return uma matriz de tamanho [n+1][2(n - 1)] contendo a solução inicial,
     * onde n é quantidade de times presentes no parâmetro table
     * @throws NullPointerException
     */
    private static int[][] geraSolucaoInicial(int[][] table) throws NullPointerException
    {
        int [][] s = new int[table.length+1][2 *(table.length - 1)];

        /* Construção da solução inicial através de backtracking */
        if(!solucaoBacktracking(s, 1, 0))
            return null;
        return s;
    }

    /**
     * Solução principal para a criação de uma solução via backtracking
     * 
     * @param s         matriz solução atual
     * @param time      time a ser analisado
     * @param rodada    rodada de comparação
     * @return true ou false caso a solução seja aceita pelo backtracking
     */
    private static boolean solucaoBacktracking(int[][] s, int time, int rodada)
    {

        /**
         * Passo base do backtracking
         * O backtracking ocorrerá somente até a metade das rodadas.
         * As restantes serão espelhadas com mando de campo invertido.
         */
        if(rodada == (s[0].length / 2))
        {
            if( time == s.length - 1)
            {
                return true;
            }
            if( solucaoBacktracking(s, time+1, 0))
            {
                return true;
            }else
            {
                return false;
            }
        }

        /* Chamada recursiva pra caso T em R já possua jogo */
        if( s[time][rodada] != SEM_TIME )
        {
            if( solucaoBacktracking(s, time, rodada + 1))
            {
                return true;
            }else
            {
                return false;
            }
        }

        /* Verificação de posições válidas para cada tupla (T, T', R) */
        for(int adv = 1; adv < s.length; adv++)
        {
            if(respeitaRestricoes(s, time, adv, rodada))
            {

                /**
                 * Preenchendo apenas a primeira metade das rodadas.
                 * A segunda será espelhada com mando de campo invertido.
                 */
                int m_round = (s[0].length / 2);
                int mult = 0;

                if( (new Random()).nextInt(2) == 1)
                {
                    mult = -1; /* T joga fora de casa */
                }else
                {
                    mult = 1;
                }

                s[time][rodada] = mult * adv;
                s[adv][rodada] = -mult * time;
                s[time][rodada + m_round] = -mult * adv;
                s[adv][rodada + m_round] = mult * time;

                /* Backtracking para a próxima rodada */
                if(solucaoBacktracking(s, time, rodada+1))
                    return true;
                    
                /* Caso o backtracking para T, R+1 seja falseo, desfazer as alterações */
                s[time][rodada] = SEM_TIME;
                s[adv][rodada] = SEM_TIME;
                s[time][rodada + m_round] = SEM_TIME;
                s[adv][rodada + m_round] = SEM_TIME;
            }
        }
      

        return false;
    }

    /**
     * Verifica se as restrições de viabilidade da solução estão sendo respeitadas
     * 
     * @param s             matriz solução atual
     * @param time          time a ser analisado
     * @param adversario    adversário do time
     * @param rodada        rodada de comparação
     * @return se determinada combinação de time, adversário e rodada
     * respeitam as restrições
     */
    private static boolean respeitaRestricoes(int[][] s, int time, int adversario, int rodada)
    {
        if(time == adversario)
            return false;

        /* Adversario já está jogando na rodada atual */
        if(s[adversario][rodada] != SEM_TIME)
            return false;
        
        /* Time já jogou na rodada atual */
        if(s[time][rodada] != SEM_TIME)
            return false;
        
        /* Time já jogou contra adversário */
        for(int rodadas = 0; rodadas < s[0].length / 2; rodadas++)
        {
            if( Math.abs(s[time][rodadas]) == adversario)
                return false;
        }

        return true;
    }

    /**
     * Cacula da o custo da Função Objetivo de uma determinada solução
     * considerando a quantidade de violações cometidas e um peso de punição.
     * 
     * @param table         tabela de distância
     * @param s             matriz de solução atual
     * @param num_violacoes número de violações cometidas pela solução s
     * @param peso          peso multiplicador na função de punição
     * @return o valor (double) da função objetivo da solução atual
     */
    public static double custo(int[][] table, int[][] s, int num_violacoes, float peso)
    {
        double fo = 0;
        // int peso = 1;
        
        for(int times = 1; times < s.length; times++)
        {
            fo += table[times - 1][ Math.abs(s[times][0]) - 1];
            int atual = Math.abs(s[times][0]) - 1;

            for(int rodadas = 0; rodadas < s[0].length - 1; rodadas++)
            {
                int prx = Math.abs(s[times][rodadas+1]) - 1;

                fo += table[atual][prx];

                atual = prx;
            }
            fo += table[ Math.abs(s[times][s[0].length - 1]) - 1 ][times - 1];
        }

        if(num_violacoes != 0)
        {
            fo = Math.sqrt( (fo * fo) + 
            (peso * Math.pow((double)( 1 + Math.sqrt(num_violacoes) * Math.log( num_violacoes / 2)), (double)2) ));
        }
        return fo;
    }

    /**
     * Verifica se a solução respeita as restrições de
     * não-repetição (T não pode jogar 2x em sequência contra T')
     * e de máxima de jogos dentro/fora de casa.
     * 
     * @param s matriz de solução atual
     * @return  número total de violações
     */
    public static int violacoes(int[][] s)
    {
        int num_violacoes = 0;

        for(int time = 1; time < s.length; time++)
        {
            int adversaio = Math.abs(s[time][0]), 
            dentro = (s[time][0] > 0)? 1 : 0, 
            fora = (s[time][0] < 0)? 1 : 0;
            for(int rodada = 1; rodada < s[0].length; rodada++)
            {
                if( Math.abs(s[time][rodada]) == adversaio )
                {
                    num_violacoes++;
                }
                adversaio = Math.abs(s[time][rodada]);

                if( dentro < 3 )
                {
                    if(s[time][rodada] > 0)
                    {
                        dentro++;
                    }else
                    {
                        dentro = 0;
                    }
                }else
                {
                    if(s[time][rodada] > 0)
                    {
                        num_violacoes++;
                    }else
                    {
                        dentro = 0;
                    }
                }

                if( fora < 3 )
                {
                    if(s[time][rodada] < 0)
                    {
                        fora++;
                    }else
                    {
                        fora = 0;
                    }
                }else
                {
                    if(s[time][rodada] < 0)
                    {
                        num_violacoes++;
                    }else
                    {
                        fora = 0;
                    }
                }

            }
        }
        return num_violacoes;
    }

    /**
     * Geração de solução vizinha s' de s
     * Seleciona randomicamente uma entre quatro
     * possibilidades de vizinhança
     * 
     * @param table tabela de distância
     * @param s     matriz solução atual
     * @return solução vizinha s'
     */
    private static int[][] geraVizinho(int[][] table, int[][] s)
    {
        int [][] s_;
        boolean valido;

        do
        {
            valido = true;
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

            /* Verificando se o vizinho respeita a restrição de não repetição */
            for(int time = 1; time < s.length; time++)
            {
                int adv = Math.abs(s[time][0]);
                for(int rodada = 1; rodada < s[0].length; rodada++)
                {
                    if(Math.abs(s[time][rodada]) == adv)
                    {
                        valido = false;
                        break;
                    }else
                    {
                        adv = Math.abs(s[time][rodada]);
                    }
                }
            }

        }while(!valido);

        return s_;
    }

    /**
     * Troca o mando de casa entre dois times T e T'
     * escolhidos aleatoriamente.
     * 
     * @param s matriz solução atual
     * @return s com as alterações de mando de casa
     */
    private static int[][] trocaCasa(int[][] s)
    {
        int time, adversario, rodada;
        Random rng = new Random();

        time = 1 + rng.nextInt(s.length - 1);
        rodada = rng.nextInt(s[0].length/2);

        adversario = Math.abs(s[time][rodada]);

        /* Trocando o mando de campo de T e T' */
        for(int rodadas = 0; rodadas < s[0].length; rodadas++)
        {
            if(s[time][rodadas] == -adversario)
            {
                s[time][rodadas] *= -1;
                s[adversario][rodadas] *= -1;

                s[time][rodada] *= -1;
                s[adversario][rodada] *= -1;
                break;
            }
        }

        return s;
    }

    /**
     * Troca duas rodadas selecionadas aleatoriamente
     * 
     * @param s matriz solução atual
     * @return s com as rodadas R e R' alteradas
     */
    private static int[][] trocaRodada(int[][] s)
    {
        /* Troca duas rodadas randomicamente */
        int r1, r2;
        Random rng = new Random();
        
        r1 = rng.nextInt(s[0].length);
        r2 = rng.nextInt(s[0].length);

        /* Trocando as rodadas r1 e r2 */
        for(int times = 1; times < s.length; times++)
        {
            int aux = s[times][r1];
            s[times][r1] = s[times][r2];
            s[times][r2] = aux;
        }
        return s;
    }

    /**
     * Escolhe aleatoriamente dois times T e T'
     * e troca a sequência de jogos entre ambos.
     * A única exceção é q T/T' não jogará contra si mesmo
     * ao trocar as agendas de jogos. Após isso, a tabela
     * é arrumada.
     * 
     * @param s matriz solução atual
     * @return s com a sequência de jogos de T e T'
     * alteradas
     */
    private static int[][] trocaTime(int[][] s)
    {
        /**
         * Troca a lista de times que T jogaria
         * com a lista de A
         */
        int time, adversario;
        Random rng = new Random();

        time = 1 + rng.nextInt(s.length - 1);
        adversario = time;
        
        /* Certificando que os dois times são diferentes */
        while(time == adversario) adversario = 1 + rng.nextInt(s.length - 1);
        
        for(int rodada = 0; rodada < s[0].length; rodada++)
        {
            if(Math.abs(s[time][rodada]) == adversario) continue;
            else
            {
                int aux = s[time][rodada];
                s[time][rodada] = s[adversario][rodada];
                s[adversario][rodada] = aux;

                /* Arrumando o restante de cada rodada */
                aux = Math.abs(s[time][rodada]);
                s[aux][rodada] = (s[time][rodada] < 0)? time : -time;

                aux = Math.abs(s[adversario][rodada]);
                s[aux][rodada] = (s[adversario][rodada] < 0)? adversario : -adversario;

            }
        }

        return s;
    }

    /**
     * Seleciona aleatorimante uma rodada e dois times.
     * Troca os adversários de cada time naquela rodada escolhida
     * e altera a tabela para que não haja conflitos
     * 
     * @param s matriz solução atual
     * @return  s com as alterações de troca parcial das
     * agendas de jogos dos times T e T'
     */
    private static int[][] trocaParcialTime(int[][] s)
    {
        int time, time_;
        int rodada, aux;
        Random rng = new Random();

        time = 1 + rng.nextInt(s.length - 1);
        time_ = time;

        while(time == time_) time_ = 1 + rng.nextInt(s.length - 1);

        rodada = rng.nextInt(s[0].length);

        while( Math.abs(s[time][rodada]) == time_ ) rodada = rng.nextInt(s[0].length);
                
        while(rodada != -1)
        {
            /* Trocar com T' em R[T vs adv] */
            aux = s[ time][rodada];
            s[time][rodada] = s[time_][rodada];
            s[time_][rodada] = aux;

            /* Arrumando o restante da rodada */
            aux = Math.abs(s[time][rodada]);
            s[aux][rodada] = (s[time][rodada] < 0)? time : -time;

            aux = Math.abs(s[time_][rodada]);
            s[aux][rodada] = (s[time_][rodada] < 0)? time_ : -time_;

            /* Procura rodada em que 'time' joga contra 'adv' */
            rodada = procuraRodada(s, time, s[time][rodada], rodada);
        }

        return s;
    }

    /**
     * Procura dentro dentro da solução s qual rodada
     * o time t1 jogou contra t2, desde que não seja a
     * rodada especificada.
     * 
     * @param s         matriz solução atual
     * @param t1        time 1 para comparação
     * @param t2        time 2 para comparação
     * @param rodada    rodada a ser evitadda
     * @return          rodada em que t1 joga contra t2
     * ou -1 caso não haja nenhuma.
     */
    private static int procuraRodada(int[][] s, int t1, int t2, int rodada)
    {
        for(int rodadas = 0; rodadas < s[0].length; rodadas++)
        {
            if( s[t1][rodadas] == t2 && rodadas != rodada )
                return rodadas;
        }
        return -1;
    }

    /**
     * Faz uma cópia da solução sem referenciar o
     * objeto origial. Somente o valor.
     * 
     * @param s     matriz solução
     * @return      s' cópia de s
     */
    private static int[][] copiaSolucao(int[][] s){
        int[][] s_ = new int[s.length][s[0].length];

        for(int i = 1; i < s.length; i++){
            for(int j = 0; j < s[0].length; j++){
                s_[i][j] = s[i][j];
            }
        }

        return s_;
    }
}
