package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import structs.client.Stud;

public class ParallelInsertTest {

    private static final int NUM_CLIENTES = 1000;
    private static final int NUM_PRODUTOS = 10;

    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition startCondition  = lock.newCondition();
    private static final Condition finishCondition = lock.newCondition();

    private static boolean ready = false;
    private static int finished = 0;

    public static void main(String[] args) throws Exception {

        Stud[] studs = new Stud[NUM_CLIENTES];
        Thread[] threads = new Thread[NUM_CLIENTES];

        BufferedWriter logFile = new BufferedWriter(
                new FileWriter("resultados_test.txt"));
        logFile.write("CLIENTE;RESPOSTA\n");

        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int clienteId = i;
            studs[i] = new Stud();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    //System.out.println("[DEBUG] Cliente " + clienteId + " a fazer login...");
                    studs[clienteId].sendLOGIN(
                            TipoMsg.LOGIN,
                            "tiago",
                            "tiago" 
                    );
                    //System.out.println("[DEBUG] Cliente " + clienteId + " login enviado");

                    // Espera pelo sinal de arranque
                    lock.lock();
                    try {
                        while (!ready) {
                            //System.out.println("[DEBUG] Cliente " + clienteId + " à espera do GO");
                            startCondition.await();
                        }
                    } finally {
                        lock.unlock();
                    }

                    //System.out.println("[DEBUG] Cliente " + clienteId + " iniciou bombardeamento");

                    // Bombardeamento de eventos
                    for (int j = 0; j < NUM_PRODUTOS; j++) {
                        Thread.sleep(1); // Pequena pausa para evitar sobrecarga total
                        studs[clienteId].sendEVENTO(
                                TipoMsg.REGISTO,
                                "produto_" + j,
                                1,
                                1.0
                        );
                        //System.out.println("[DEBUG] Cliente " + clienteId + " enviou produto_" + j);
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        //System.out.println("[DEBUG] Cliente " + clienteId + " terminou. Total finished: " + finished);
                        if (finished == NUM_CLIENTES) {
                            finishCondition.signal();
                            //System.out.println("[DEBUG] Último cliente sinalizou fim");
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
            //System.out.println("[DEBUG] Sinal de arranque enviado para todos os clientes");
        } finally {
            lock.unlock();
        }

        // Espera até todos terminarem
        lock.lock();
        try {
            while (finished < NUM_CLIENTES) {
                finishCondition.await();
            }
            //System.out.println("[DEBUG] Todos os clientes terminaram envio de eventos");
        } finally {
            lock.unlock();
        }

        // Recolhe respostas
        synchronized (logFile) {
            for (int i = 0; i < NUM_CLIENTES; i++) {
                List<String> replies = studs[i].getRepliesList();
                for (String r : replies) {
                    logFile.write(i + ";" + r + "\n");
                    //System.out.println("[DEBUG] Cliente " + i + " recebeu resposta: " + r);
                }
            }
            logFile.flush();
        }

        // Cleanup
        for (Stud s : studs) {
            s.close();
        }

        logFile.close();
        System.out.println("Teste concluído com sucesso!");
    }
}
