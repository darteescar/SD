package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Teste de escalabilidade robusto */
public class RobustScalabilityTestFinal {

    /** Lock para sincronização das threads */
    private final ReentrantLock lock = new ReentrantLock();

    /** Condição para sinalizar o início do envio de mensagens */
    private final Condition startCondition = lock.newCondition();
    
    /** Condição para sinalizar o término do envio de mensagens */
    private final Condition finishCondition = lock.newCondition();

    /** Pausa entre envio de mensagens em milissegundos */
    private static final int PAUSA_EVENTO_MS = 5;

    /** Variável de controlo para iniciar o envio de mensagens */
    private boolean ready = false;

    /** Contador de threads que terminaram o envio */
    private int finished = 0;

    /** 
     * Envia eventos de múltiplos clientes em paralelo e regista estatísticas num ficheiro de log
     * 
     * @param num_clientes Número de clientes a simular
     * @param num_produtos Número de produtos por cliente
     * @param logFilePath Caminho do ficheiro de log onde as estatísticas serão gravadas
     * @throws Exception Em caso de erro durante o envio ou gravação do log
     */
    private void enviar_Eventos(int num_clientes, int num_produtos, String logFilePath) throws Exception {

        System.out.println("[INFO] Iniciando ronda com " + num_clientes + " clientes e " + num_produtos + " produtos por cliente.");

        // Reset de contadores
        ready = false;
        finished = 0;

        Stud2[] studs = new Stud2[num_clientes];
        Thread[] threads = new Thread[num_clientes];

        for (int i = 0; i < num_clientes; i++) {
            final int clienteId = i;
            studs[i] = new Stud2();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    // Login
                    studs[clienteId].sendLOGIN(TipoMsg.LOGIN, "tiago", "tiago");

                    studs[clienteId].sendNotificacaoVC(TipoMsg.NOTIFICACAO_VC, 5 );

                    studs[clienteId].sendNotificacaoVS(TipoMsg.NOTIFICACAO_VS, "produto1", "produto5" );

                    // Espera pelo sinal de arranque
                    lock.lock();
                    try {
                        while (!ready) startCondition.await();
                    } finally {
                        lock.unlock();
                    }

                    // Bombardeamento de eventos
                    for (int j = 0; j < num_produtos; j++) {
                        Thread.sleep(PAUSA_EVENTO_MS);
                        studs[clienteId].sendEVENTO(
                                TipoMsg.REGISTO,
                                "produto" + j,
                                1,
                                1.0
                        );
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        if (finished == num_clientes) finishCondition.signal();
                    } finally {
                        lock.unlock();
                    }

                } catch (Exception e) {
                    System.out.println("[ERROR] Cliente " + clienteId + " exception:");
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        // Sinal de START
        lock.lock();
        try {
            ready = true;
            startCondition.signalAll();
        } finally {
            lock.unlock();
        }

        // Espera até todos terminarem
        lock.lock();
        try {
            while (finished < num_clientes) finishCondition.await();
        } finally {
            lock.unlock();
        }

        System.out.println("[INFO] Todos os clientes terminaram de enviar eventos. Aguardando processamento final...");

        Thread.sleep(1000); // espera 1 segundo

        System.out.println("[INFO] Gravando estatísticas da ronda no log...");

        // Grava no log em modo append a média da ronda
        try (BufferedWriter logFile = new BufferedWriter(new FileWriter(logFilePath, true))) {
            logFile.write("----- Estatísticas da Ronda -----\n");

            // Mapa para acumular somas e contagens por tipo de mensagem
            Map<TipoMsg, Long> sumPorTipo = new HashMap<>();
            Map<TipoMsg, Integer> countPorTipo = new HashMap<>();

            for (int i = 0; i < num_clientes; i++) {
                Map<TipoMsg, List<Long>> timestamps = studs[i].getMessageTimestamps();

                for (Map.Entry<TipoMsg, List<Long>> entry : timestamps.entrySet()) {
                    TipoMsg tipo = entry.getKey();
                    List<Long> tempos = entry.getValue();

                    long sum = tempos.stream().mapToLong(Long::longValue).sum();
                    int count = tempos.size();

                    sumPorTipo.put(tipo, sumPorTipo.getOrDefault(tipo, 0L) + sum);
                    countPorTipo.put(tipo, countPorTipo.getOrDefault(tipo, 0) + count);
                }
            }

            // Calcula a média global por tipo de mensagem
            for (TipoMsg tipo : sumPorTipo.keySet()) {
                long totalSum = sumPorTipo.get(tipo);
                int totalCount = countPorTipo.get(tipo);
                long average = totalCount > 0 ? totalSum / totalCount : 0;

                logFile.write("TipoMensagem: " + tipo + "; TempoMedioRonda(ms): " + average + "\n");
            }

            logFile.write("----- Ronda com " + num_clientes + " clientes e " + num_produtos + " produtos por cliente -----\n");
            logFile.write("Total de eventos enviados: " + (num_clientes * num_produtos) + "\n");
            int x = 0;
            for (int i = 0 ; i < num_clientes; i++) {
                List<String> replies = studs[i].getRepliesList();
                x += replies.size();
            }
            logFile.write("Eventos recebidos: " + x + "\n");

            logFile.write("----- Fim da Ronda -----\n");

            logFile.write("\n");
            logFile.write("\n");
        }
        // Fecha os studs
        for (Stud2 stud : studs) stud.close();

        System.out.println("[INFO] Rodada concluída e log gravado");
    }

    /** 
     * Envia queries de múltiplos clientes em paralelo e regista estatísticas num ficheiro de log
     * 
     * @param num_clientes Número de clientes a simular
     * @param num_produtos Número de produtos por cliente
     * @param logFilePath Caminho do ficheiro de log onde as estatísticas serão gravadas
     */
    private void enviar_Querys(int num_clientes, int num_produtos, String logFilePath) throws Exception {
        // Reset de contadores
        ready = false;
        finished = 0;

        System.out.println("[INFO] Iniciando ronda de querys com " + num_clientes + " clientes e " + num_produtos + " produtos por cliente.");

        Stud2[] studs = new Stud2[num_clientes];
        Thread[] threads = new Thread[num_clientes];

        for (int i = 0; i < num_clientes; i++) {
            final int clienteId = i;
            studs[i] = new Stud2();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    // Login
                    studs[clienteId].sendLOGIN(TipoMsg.LOGIN, "tiago", "tiago");

                    // Espera pelo sinal de arranque
                    lock.lock();
                    try {
                        while (!ready) startCondition.await();
                    } finally {
                        lock.unlock();
                    }

                    // Bombardeamento de eventos
                    for (int j = 0; j < num_produtos; j++) {
                        Thread.sleep(PAUSA_EVENTO_MS);
                        switch (j%5) {
                            case 0:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.PRECO_MAXIMO,
                                        "produto" + j,
                                        3
                                );  break;
                            case 1:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.PRECO_MEDIO,
                                        "produto" + j,
                                        3
                                );  break;
                            case 2:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.VOLUME_VENDAS,
                                        "produto" + j,
                                        3
                                );  break;
                            case 3:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.QUANTIDADE_VENDAS,
                                        "produto" + j,
                                        3
                                );  break;
                            default:
                                studs[clienteId].sendFILTRAR(
                                        TipoMsg.LISTA,
                                        new ArrayList<String>(List.of("produto" + j, "produto" + (j+1), "produto" + (j+2))),
                                        3
                                );  break;
                        }
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        if (finished == num_clientes) finishCondition.signal();
                    } finally {
                        lock.unlock();
                    }

                } catch (Exception e) {
                    System.out.println("[ERROR] Cliente " + clienteId + " exception:");
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        // Sinal de START
        lock.lock();
        try {
            ready = true;
            startCondition.signalAll();
        } finally {
            lock.unlock();
        }

        // Espera até todos terminarem
        lock.lock();
        try {
            while (finished < num_clientes) finishCondition.await();
        } finally {
            lock.unlock();
        }

        System.out.println("[INFO] Todos os clientes terminaram de enviar queries. Aguardando processamento final...");

        Thread.sleep(10000); // Espera adicional para garantir que todas as respostas foram recebidas

        // Grava no log em modo append a média da ronda
        try (BufferedWriter logFile = new BufferedWriter(new FileWriter(logFilePath, true))) {
            logFile.write("----- Estatísticas da Ronda -----\n");

            // Mapa para acumular somas e contagens por tipo de mensagem
            Map<TipoMsg, Long> sumPorTipo = new HashMap<>();
            Map<TipoMsg, Integer> countPorTipo = new HashMap<>();

            for (int i = 0; i < num_clientes; i++) {
                Map<TipoMsg, List<Long>> timestamps = studs[i].getMessageTimestamps();

                for (Map.Entry<TipoMsg, List<Long>> entry : timestamps.entrySet()) {
                    TipoMsg tipo = entry.getKey();
                    List<Long> tempos = entry.getValue();

                    long sum = tempos.stream().mapToLong(Long::longValue).sum();
                    int count = tempos.size();

                    sumPorTipo.put(tipo, sumPorTipo.getOrDefault(tipo, 0L) + sum);
                    countPorTipo.put(tipo, countPorTipo.getOrDefault(tipo, 0) + count);
                }
            }

            // Calcula a média global por tipo de mensagem
            for (TipoMsg tipo : sumPorTipo.keySet()) {
                long totalSum = sumPorTipo.get(tipo);
                int totalCount = countPorTipo.get(tipo);
                long average = totalCount > 0 ? totalSum / totalCount : 0;

                logFile.write("Tipo Mensagem: " + tipo + "; Tempo Medio(ms): " + average + "\n");
            }

            logFile.write("----- Ronda com " + num_clientes + " clientes e " + num_produtos + " produtos por cliente -----\n");
            logFile.write("Total de eventos enviados: " + (num_clientes * num_produtos) + "\n");
            int x = 0;
            for (int i = 0 ; i < num_clientes; i++) {
                List<String> replies = studs[i].getRepliesList();
                x += replies.size();
            }
            logFile.write("Eventos recebidos: " + x + "\n");

            logFile.write("----- Fim da Ronda -----\n");

            logFile.write("\n");
            logFile.write("\n");
        }

        // Fecha os studs
        for (Stud2 stud : studs) {
            //List <String> replies = stud.getRepliesList();
            //System.out.println("[INFO] Cliente recebeu " + replies );

            stud.close();
        }

        System.out.println("[INFO] Rodada concluída e log gravado em " + logFilePath);

    }

    /** 
     * Ponto de entrada do teste de escalabilidade robusto. Pede ao utilizador os parâmetros do teste e executa múltiplas rondas de envio de eventos e queries.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     * @throws Exception Em caso de erro durante o teste
     */
    public static void main(String[] args) throws Exception {
        RobustScalabilityTestFinal test = new RobustScalabilityTestFinal();
        String logFilePath = "src/scripts/results/robust_scalability_test_final_log.txt";
        new FileWriter(logFilePath, false).close(); // Limpa o ficheiro de log antes de começar

        Scanner scanner = new Scanner(System.in);

        System.out.print("Insira o número de clientes: ");
        int num_clientes = scanner.nextInt();

        System.out.print("Insira o número de produtos: ");
        int num_produtos = scanner.nextInt();

        System.out.print("Insira o intervalo entre rodadas de eventos (ms): ");
        int pausa_evento_ms = scanner.nextInt();

        System.out.print("Insira o número de rodadas: ");
        int num_rodadas = scanner.nextInt();

        for (int i = 1 ; i <= num_rodadas; i++) {
            System.out.println("Iniciando ronda " + i + " de " + num_rodadas);
            test.enviar_Eventos(num_clientes, num_produtos, logFilePath);
            num_clientes *= 2;
            Thread.sleep(pausa_evento_ms);
        }

        System.out.println("Ronda concluída. Deseja enviar queries? (s/n)");
        String resposta = scanner.next();
        if (resposta.equalsIgnoreCase("s")) {
            System.out.print("Insira o número de clientes para queries: ");
            int num_clientes_query = scanner.nextInt();

            System.out.print("Insira o número de produtos para queries: ");
            int num_produtos_query = scanner.nextInt();

            test.enviar_Querys(num_clientes_query, num_produtos_query, logFilePath);
        }

        scanner.close();
        System.out.println("Teste concluído. Log gravado em: " + logFilePath);
    }

}
