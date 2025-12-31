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

    private static final int PAUSA_EVENTO_MS = 1;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition startCondition = lock.newCondition();
    private final Condition finishCondition = lock.newCondition();

    private boolean ready = false;
    private int finished = 0;

    public void enviar_Eventos(int num_clientes, int num_produtos, String logFilePath) throws Exception {

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

        Thread.sleep(5000); // Espera adicional para garantir que todas as respostas foram recebidas

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

    private void enviar_Querys(int num_clientes, int num_produtos, String logFilePath) throws Exception {
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
                                        5
                                );  break;
                            case 1:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.PRECO_MEDIO,
                                        "produto" + j,
                                        5
                                );  break;
                            case 2:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.VOLUME_VENDAS,
                                        "produto" + j,
                                        5
                                );  break;
                            case 3:
                                studs[clienteId].sendAGREGACAO(
                                        TipoMsg.QUANTIDADE_VENDAS,
                                        "produto" + j,
                                        5
                                );  break;
                            default:
                                studs[clienteId].sendFILTRAR(
                                        TipoMsg.LISTA,
                                        new ArrayList<String>(List.of("produto" + j, "produto" + (j+1), "produto" + (j+2))),
                                        5
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

        Thread.sleep(5000); // Espera adicional para garantir que todas as respostas foram recebidas

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

    public static void main(String[] args) {
        RobustScalabilityTestFinal test = new RobustScalabilityTestFinal();
        String logFilePath = "src/scripts/results/robust_scalability_test_final_log.txt";

        int num_clientes = 10;
        int num_produtos = 1000;

        try {
            for (int i = 0 ; i < 5; i++) {
                test.enviar_Eventos(num_clientes, num_produtos, logFilePath);
                Thread.sleep(30000); // Pausa de 35 segundos entre rondas (30 + 5 segundos de espera em enviar_Eventos)
                num_clientes *= 2; // Dobra o número de clientes para a próxima ronda
            }
            // 1ª ronda: 10 clientes x 1000 produtos = 10.000 eventos
            // 2ª ronda: 20 clientes x 1000 produtos = 20.000
            // 3ª ronda: 40 clientes x 1000 produtos = 40.000 eventos
            // 4ª ronda: 80 clientes x 1000 produtos = 80.000 eventos
            // 5ª ronda: 160 clientes x 1000 produtos = 160.000 eventos
            
            // esperar alguns dias
            //Thread.sleep()

            // ao usar enviar querys, garantir que o numero é multiplo de 5

        } catch (Exception e) {
            e.printStackTrace();
        }





    }

    
    //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// //////////////////////////////////////////////////
    /// 
    /// nao esquecer que como as threads apenas enviam e avisam que acabram de enviar, dependendo do numero de server workers
    /// /// pode ser que o servidor ainda esteja a processar 1 ou outro evento quando o teste acabar e por isso
    /// ao inves de ter todos os eventos processados, pode faltar 1 ou outro
}
