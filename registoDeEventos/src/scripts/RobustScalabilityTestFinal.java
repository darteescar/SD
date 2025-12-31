package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RobustScalabilityTestFinal {

    private static final int PAUSA_EVENTO_MS = 5;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition startCondition = lock.newCondition();
    private final Condition finishCondition = lock.newCondition();

    private boolean ready = false;
    private int finished = 0;

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

        Thread.sleep(1000); // Espera adicional para garantir que todas as respostas foram recebidas

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

        System.out.println("[INFO] Rodada concluída e log gravado em " + logFilePath);

    }

    public static void main(String[] args) throws Exception {
        RobustScalabilityTestFinal test = new RobustScalabilityTestFinal();
        String logFilePath = "src/scripts/results/robust_scalability_test_final_log.txt";
        new FileWriter(logFilePath, false).close(); // Limpa o ficheiro de log antes de começar

        int num_clientes = 5;
        int num_produtos = 1000;

        for (int i = 1 ; i <= 4; i++) {
            test.enviar_Eventos(num_clientes, num_produtos, logFilePath);
            Thread.sleep(35000); // Pausa de 40 segundos entre rondas (40)
            num_clientes *= 2; // Dobra o número de clientes para a próxima ronda
        }
        // 1ª ronda: 5 clientes x 1000 produtos = 5.000 eventos
        // 2ª ronda: 10 clientes x 1000 produtos = 10.000
        // 3ª ronda: 20 clientes x 1000 produtos = 20.000 eventos
        // 4ª ronda: 40 clientes x 1000 produtos = 40.000 eventos
        
        // esperar alguns dias
        //Thread.sleep()

        // ao usar enviar querys, garantir que o numero é multiplo de 5

        test.enviar_Querys(10, 10, logFilePath);




    }
}
