package scripts;

import entities.Mensagem;
import enums.TipoMsg;
import entities.payloads.Evento;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ParallelInsertTest {

    private static final int NUM_CLIENTES = 10;
    private static final int NUM_PRODUTOS = 20;
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    private static final int WAIT_AFTER_INSERT_MS = 5000; // espera 5 segundos

    public static void main(String[] args) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(NUM_CLIENTES);

        // Cria um ficheiro para logar resultados
        BufferedWriter logFile = new BufferedWriter(new FileWriter("resultados_test.txt"));
        logFile.write("ID_CLIENTE;PRODUTO;RESPOSTA\n");

        Thread[] clientes = new Thread[NUM_CLIENTES];
        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int clientId = i;
            clientes[i] = new Thread(() -> {
                try {
                    runCliente(clientId, logFile);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            clientes[i].start();
        }

        // Espera todas as threads terminarem
        latch.await();

        logFile.close();
        System.out.println("Todos os clientes finalizaram. Resultados gravados em 'resultados_test.txt'");
    }

    private static void runCliente(int clientId, BufferedWriter logFile) throws IOException, InterruptedException {
        Socket socket = new Socket(HOST, PORT);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        System.out.println("[CLIENTE " + clientId + "] Conectado");

        // --- Login fictício ---
        sendLogin(dos, clientId);

        // --- Inserir produtos ---
        Map<String, String> respostasRecebidas = new HashMap<>();
        for (int i = 1; i <= NUM_PRODUTOS; i++) {
            Evento evento = new Evento("produto_" + i, 1, 1.0);
            sendEvento(dos, evento);

            // Recebe resposta do servidor (pode chegar fora de ordem)
            Mensagem resposta = Mensagem.deserialize(dis);
            if (resposta != null) {
                respostasRecebidas.put(evento.getProduto(), new String(resposta.getData()));
            }
        }

        // --- Espera para garantir que todas as mensagens chegam ---
        Thread.sleep(WAIT_AFTER_INSERT_MS);

        // --- Grava os resultados no ficheiro ---
        synchronized (logFile) {
            for (Map.Entry<String, String> entry : respostasRecebidas.entrySet()) {
                logFile.write(clientId + ";" + entry.getKey() + ";" + entry.getValue() + "\n");
            }
            logFile.flush();
        }

        socket.close();
        System.out.println("[CLIENTE " + clientId + "] Finalizado");
    }

    private static void sendLogin(DataOutputStream dos, int clientId) throws IOException {
        String username = "usuario" + clientId;
        String password = "senha" + clientId;
        byte[] payload = (username + ":" + password).getBytes();

        Mensagem msg = new Mensagem(clientId, TipoMsg.LOGIN, payload);
        msg.serialize(dos);
        dos.flush();
    }

    private static void sendEvento(DataOutputStream dos, Evento evento) throws IOException {
        byte[] payload = evento.serialize(); // supondo que Evento tem serialize()
        Mensagem msg = new Mensagem(0, TipoMsg.REGISTO, payload);
        msg.serialize(dos);
        dos.flush();
    }
}
