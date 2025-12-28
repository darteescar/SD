package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.Stud;

public class ParallelDeterministicTest {

    private static final int NUM_CLIENTES = 5;
    private static final int NUM_PRODUTOS = 10; // número de produtos a registar
    private static final int NUM_EVENTOS = 10; // quantidade de eventos por produto

    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition startCondition = lock.newCondition();
    private static final Condition finishCondition = lock.newCondition();

    private static boolean ready = false;
    private static int finished = 0;

    public static void main(String[] args) throws Exception {

        Stud[] studs = new Stud[NUM_CLIENTES];
        Thread[] threads = new Thread[NUM_CLIENTES];

        BufferedWriter logFile = new BufferedWriter(new FileWriter("src/scripts/results/resultados_deterministicos.txt"));
        logFile.write("CLIENTE;TIPO;PRODUTO;RESPOSTA\n");

        // Produtos determinísticos
        List<String> produtos = new ArrayList<>();
        for (int p = 0; p < NUM_PRODUTOS; p++) {
            produtos.add("produto" + p);
        }

        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int clienteId = i;
            studs[i] = new Stud();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    // Login determinístico
                    studs[clienteId].sendLOGIN(TipoMsg.LOGIN, "tiago", "tiago");

                    // Subscrições VC: todos os clientes para todos os produtos
                    for (String produto : produtos) {
                        studs[clienteId].sendNotificacaoVC(TipoMsg.NOTIFICACAO_VC, NUM_EVENTOS);
                    }

                    // Subscrições VS: todos os clientes para pares de produtos
                    for (int j = 0; j < produtos.size() - 1; j++) {
                        studs[clienteId].sendNotificacaoVS(TipoMsg.NOTIFICACAO_VS, produtos.get(j), produtos.get(j + 1));
                    }

                    // Espera pelo sinal de arranque
                    lock.lock();
                    try {
                        while (!ready) startCondition.await();
                    } finally {
                        lock.unlock();
                    }

                    // Bombardeamento de eventos
                    for (String produto : produtos) {
                        for (int e = 0; e < NUM_EVENTOS; e++) {
                            Thread.sleep(5); // pequeno delay para evitar sobrecarga
                            studs[clienteId].sendEVENTO(TipoMsg.REGISTO, produto, 1, 1.0);
                        }
                    }

                    // Espera 15s para garantir que o dia avançou
                    Thread.sleep(15000);

                    // Envia queries determinísticas
                    for (String produto : produtos) {
                        studs[clienteId].sendAGREGACAO(TipoMsg.VOLUME_VENDAS, produto, 1);
                        studs[clienteId].sendAGREGACAO(TipoMsg.QUANTIDADE_VENDAS, produto, 1);
                        studs[clienteId].sendAGREGACAO(TipoMsg.PRECO_MEDIO, produto, 1);
                        studs[clienteId].sendAGREGACAO(TipoMsg.PRECO_MAXIMO, produto, 1);
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        if (finished == NUM_CLIENTES) finishCondition.signal();
                    } finally {
                        lock.unlock();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            threads[i].start();
        }

        // Sinal de start
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
            while (finished < NUM_CLIENTES) finishCondition.await();
        } finally {
            lock.unlock();
        }

        // Recolhe respostas e grava no ficheiro
        
        for (int i = 0; i < NUM_CLIENTES; i++) {
            List<String> replies = studs[i].getRepliesList();
            for (String r : replies) {
                logFile.write(i + ";" + r + "\n");
            }
        }
        logFile.flush();
        

        // Cleanup
        for (Stud s : studs) s.close();
        logFile.close();

        System.out.println("Teste determinístico concluído com sucesso!");
    }
}
