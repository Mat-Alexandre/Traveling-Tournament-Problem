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
     * @param table         tabela de distância
     * @param temp_final    temperatura final desejada
     * @param max_ite       número máximo de iterações
     * @param alfa          parâmetro do SA
     * @return
     * @throws NullPointerException
     */
    public static int[][] SimulatedAnnealing(int[][] table, float temp_final, double alfa) throws NullPointerException{
        /* Gerar a solução inicial e cálculo da temperatura */
        int[][] s = geraSolucaoInicial(table), viz = null;
        int max_ite = (s.length - 1)*((s.length - 1)-1)/2;
        float temperatura = calculaTemperaturaInicial(table, s, max_ite, alfa);
        double fo_s, fo_viz;
        
        System.out.println("Solução inicial:");
        Main.printTable(s);
        
        do{
            int i = 0;
            do{
                
                /* Gerar um vizinho aleatório */
                viz = geraVizinho(table, copiaSolucao(s));
                
                /* deltaE = Diferença entre f(s') e f(s) */
                fo_s = calculaFO(table, s);
                fo_viz = calculaFO(table, viz);
                double deltaE = fo_viz - fo_s;

                if(deltaE <= 0/* deltaE <= 0 */)
                {

                    /* Aceita o vizinho como solução */
                    s = viz;
                }    
                else
                {
                    calculaFO(table, s);
                    
                    /* s' será aceito com uma probabilidade e^(-deltaE / T) */
                    s = ((new Random()).nextDouble() <= Math.exp( (-deltaE)/temperatura ))? viz : s;
                }
                i++;
            }while(i < max_ite);
            
            /* Atualiza a temperatura */
            temperatura *= alfa;
        }while(temperatura < temp_final);

        /* Retorna a melhor solução encontrada */
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
        if(solucaoBacktracking(s, 1, 0) == false)
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
            if(time == s.length-1)
                return true;
            else if(solucaoBacktracking(s, time + 1, 0))
                return true;
            else
                return false;
        }
        
        /* Para cada time T Verificar se o time T' pode ser colocado na rodada */
        int fora_seq = 0;
        for(int adversario = 1; adversario < s.length; adversario++)
        {
            /* Se respeita as restrições */
            if(s[time][rodada] == SEM_TIME && rodada < (s[0].length / 2))
            {
                if(respeitaRestricoes(s, time, adversario, rodada))
                {
                    /* Posição válida */
                    /* Preenchendo as rodadas a partir da r[2(n - 1)] de forma espelhada */
                    int m_round = (s[0].length / 2);
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

                    s[time][rodada] = mult * adversario;
                    s[adversario][rodada] = -mult * time;
                    s[time][rodada + m_round] = -mult * adversario;
                    s[adversario][rodada + m_round] = mult * time;
                    /* Backtracking para a próxima rodada */
                    if(solucaoBacktracking(s, time, rodada + 1))
                        return true;
                        
                    /* Se chegou neste trecho, desfazer as alterações */
                    s[time][rodada] = SEM_TIME;
                    s[adversario][rodada] = SEM_TIME;
                    s[time][rodada + m_round] = SEM_TIME;
                    s[adversario][rodada + m_round] = SEM_TIME;
                }
            }
            else
            {
                rodada++;
            }
        }

        return true;
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
            if(s[time][rodadas] == adversario)
                return false;
        }

        return true;
    }

    /**
     * Cacula da o valor da Função Objetivo de uma determinada solução
     * 
     * Cálculo atual se basea apenas no custo de viajar de um local
     * ao outro sem penalizar infrações.
     * Link para calculo correto: https://mat.tepper.cmu.edu/TOURN/ttp_ipcp.pdf
     * 
     *  @param table    tabela de distância
     *  @param s        matriz solução atual
     *  @return o valor com precisão double da solução s
     */
    public static double calculaFO(int[][] table, int[][] s)
    {
        double fo = 0;
        
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
        return fo;
    }

    /**
     * Calcula a temperatura inicial para uma solução inicial s
     * A temperatura inicial será feita através desse trecho de
     * código e não via input do usuário
     * 
     * @param table     tabela de distância
     * @param s         matriz solução atual
     * @param max_ite   numero máximo permitido de iterações
     * @param alfa      parâmetro do SA
     * @return float correspondente a temperatura inicial para 
     * início da heurística Simulated Annealing
     */
    private static float calculaTemperaturaInicial(int[][] table, int [][] s, int max_ite, double alfa)
    {
        int aceitos = 0, min_aceitos;
        double fo_s, fo_viz, delta;
        float temperatura = 2;
        int[][] s_ = copiaSolucao(s);

        min_aceitos = (int)(alfa * max_ite);

        while( aceitos < min_aceitos )
        {
            int ite = 0;
            while( ite < max_ite)
            {
                ite++;
                fo_s = calculaFO(table, s_);
                fo_viz = calculaFO(table, geraVizinho(table, s_));
                delta = fo_viz - fo_s;
                if( delta < 0 )
                    aceitos++;
                else
                {
                    if( (new Random()).nextDouble() < Math.exp( (-delta)/temperatura ) )
                        aceitos++;
                }
            }
            if(aceitos < min_aceitos)
            {
                aceitos = 0;
                temperatura *= 1.1f;
            }
        }
        return temperatura;
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
        switch((new Random()).nextInt(3) + 1){
        // switch(4){
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
