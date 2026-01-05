package utils.workers.server;

import entities.Mensagem;
import entities.MensagemCorrompidaException;
import entities.ServerData;
import java.io.*;
import utils.structs.server.BoundedBuffer;
import utils.structs.server.ClientSession;

/** Thread responsável por ler mensagens dos clientes e adicioná-las ao buffer de mensagens pendentes */
public class ServerReader implements Runnable {

     /** Sessão do cliente associada a esta thread */
     private final ClientSession session;

     /** Stream de entrada para ler dados do cliente */
     private final DataInputStream input;

     /** Buffer de mensagens pendentes */
     private final BoundedBuffer<ServerData> mensagensPendentes;

     /** Identificador do cliente associado a esta thread */
     private final int cliente;

     /** 
      * Construtor da classe ServerReader
      * 
      * @param session Sessão do cliente
      * @param mensagensPendentes Buffer de mensagens pendentes
      * @param cliente Identificador do cliente
      * @param input Stream de entrada para ler dados do cliente
      * @throws IOException Se ocorrer um erro de I/O
      * @return Uma nova instância de ServerReader
      */
     public ServerReader(ClientSession session,
                         BoundedBuffer<ServerData> mensagensPendentes,
                         int cliente, 
                         DataInputStream input) throws IOException {
          this.session = session;
          this.input = input;
          this.mensagensPendentes = mensagensPendentes;
          this.cliente = cliente;
     }

     /** 
      * Método run da thread que lê mensagens do socket do cliente e as adiciona-as ao buffer de mensagens pendentes
      */
     @Override
     public void run() {
     while (true) {
          try {
               Mensagem mensagem = Mensagem.deserializeWithId(input);
               mensagensPendentes.add(new ServerData(cliente, mensagem));

          } catch (MensagemCorrompidaException e) {
               mensagensPendentes.add(new ServerData(cliente, new Mensagem(e.getId(), "Erro: Mensagem inválida ou corrompida.")));

          } catch (Exception e) {
               // Erro ao ler a mensagem - termina a thread
               break;
          }
     }
     session.close();
     }
}
