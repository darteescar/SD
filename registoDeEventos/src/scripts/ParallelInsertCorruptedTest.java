package scripts;

import enums.TipoMsg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import utils.structs.client.Stud;

/** Teste de inserção de eventos corrompidos de forma paralela (imita vários clientes a enviar mensagens)*/
public class ParallelInsertCorruptedTest {

    /** Lock para sincronização das threads */
    private static final ReentrantLock lock = new ReentrantLock();

    /** Condição para sinalizar o início do envio de eventos */
    private static final Condition startCondition  = lock.newCondition();

    /** Condição para sinalizar o término do envio de eventos */
    private static final Condition finishCondition = lock.newCondition();

    /** Variável de controlo para iniciar o envio de eventos */
    private static boolean ready = false;

    /** Contador de threads que finalizaram o envio de eventos */
    private static int finished = 0;

    /**
     * Ponto de entrada do teste. Solicita ao utilizador o número de clientes e eventos inválidos a enviar, cria os clientes, sincroniza o início do envio e regista as respostas num ficheiro.
     * 
     * @param args Argumentos da linha de comando
     * @throws Exception caso ocorra algum erro durante o teste (não é necessário tratar especificamente)
     */
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Insira o número de clientes: ");
        int numClientes = scanner.nextInt();

        System.out.print("Insira o número de eventos inválidos por cliente: ");
        int numProdutos = scanner.nextInt();

        scanner.close();

        Stud[] studs = new Stud[numClientes];
        Thread[] threads = new Thread[numClientes];

        BufferedWriter logFile = new BufferedWriter(
                new FileWriter("src/scripts/results/resultados_test_invalid.txt", false));
        logFile.write("CLIENTE;RESPOSTA\n");

        // Reset de controlo
        ready = false;
        finished = 0;

        for (int i = 0; i < numClientes; i++) {
            final int clienteId = i;
            studs[i] = new Stud();
            studs[i].start();

            threads[i] = new Thread(() -> {
                try {
                    // Login
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

                    // Envio de eventos inválidos
                    for (int j = 0; j < numProdutos; j++) {
                        studs[clienteId].sendEVENTO_INVALIDO(TipoMsg.REGISTO);
                    }

                    // Sinaliza fim
                    lock.lock();
                    try {
                        finished++;
                        if (finished == numClientes) {
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
            while (finished < numClientes) {
                finishCondition.await();
            }
        } finally {
            lock.unlock();
        }

        Thread.sleep(5000); // garante receção de todas as respostas

        // Recolhe respostas
        for (int i = 0; i < numClientes; i++) {
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
