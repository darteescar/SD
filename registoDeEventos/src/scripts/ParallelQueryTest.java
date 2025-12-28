package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.Stud;

public class ParallelQueryTest {

    private static final int NUM_CLIENTES = 10;
    private static final int NUM_PRODUTOS = 1000;

    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition startCondition  = lock.newCondition();
    private static final Condition finishCondition = lock.newCondition();

    private static boolean ready = false;
    private static int finished = 0;

    public static void main(String[] args) throws Exception {

        Stud[] studs = new Stud[NUM_CLIENTES];
        Thread[] threads = new Thread[NUM_CLIENTES];

        BufferedWriter logFile = new BufferedWriter(
                new FileWriter("src/scripts/results/resultados_test_query.txt"));
        logFile.write("CLIENTE;RESPOSTA\n");

        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int clienteId = i;
            studs[i] = new Stud();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    studs[clienteId].sendLOGIN(
                            TipoMsg.LOGIN,
                            "tiago",
                            "tiago" 
                    );

                    // Espera pelo sinal de arranque
                    lock.lock();
                    try {
                        while (!ready) {
                            startCondition.await();
                        }
                    } finally {
                        lock.unlock();
                    }

                    // Bombardeamento de eventos
                    for (int j = 0; j < NUM_PRODUTOS; j++) {
                        Thread.sleep(5); // Pequena pausa para evitar sobrecarga total
                        studs[clienteId].sendAGREGACAO(
                                TipoMsg.VOLUME_VENDAS,
                                "banana",
                                10
                        );
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        if (finished == NUM_CLIENTES) {
                            finishCondition.signal();
                        }
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
            while (finished < NUM_CLIENTES) {
                finishCondition.await();
            }
        } finally {
            lock.unlock();
        }

        // Recolhe respostas
        for (int i = 0; i < NUM_CLIENTES; i++) {
            List<String> replies = studs[i].getRepliesList();
            for (String r : replies) {
                logFile.write(i + ";" + r + "\n");
            }
        }
        logFile.flush();

        // Cleanup
        for (Stud s : studs) {
            s.close();
        }

        logFile.close();
        System.out.println("Teste concluído com sucesso!");
    }
}
