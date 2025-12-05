package structs;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import entities.Mensagem;
import enums.TipoMsg;

public class Sender implements Runnable{
    private final Demultiplexer demu;
    private final ReentrantLock lock;
    private Mensagem mToSend;
    private List<String> replies;
    private final ClienteView view;

    public Sender(Demultiplexer demu, Mensagem mToSend, List<String> replies, ClienteView view){
        this.demu = demu;
        this.lock =  new ReentrantLock();
        this.mToSend = mToSend;
        this.replies = replies;
        this.view = view;
    }

    @Override
    public void run(){
        try{
            // Enviar a mensagem
            this.demu.send(this.mToSend);

            // Esperar pela resposta (especificamente com o ID passado)
            int id = this.mToSend.getID();
            String reply = demu.receive(id);

            TipoMsg tipo = mToSend.getTipo();

            if(tipo.equals(TipoMsg.LOGIN) && reply.equals("true")){
                this.view.switchAutenticacao();
                System.out.println("[LOGIN EFETUADO COM SUCESSO]");

            }else if(tipo.equals(TipoMsg.LOGIN) && reply.equals("false")){
                System.out.println("[CLIENTE NAO REGISTADO, POR FAVOR REGISTE-SE]");

            }else if(tipo.equals(TipoMsg.REGISTA_LOGIN) && reply.equals("true")){
                this.view.switchAutenticacao();
                System.out.println("[REGISTO EFETUADO COM SUCESSO]");

            }else if (tipo.equals(TipoMsg.REGISTA_LOGIN) && reply.equals("true")){
                System.out.println("[PROBLEMA AO REGISTAR, POR FAVOR TENTE NOVAMENTE]");

            }else{
                String paraLista = "Resposta da mensagem " + id + "> " + reply;
                this.lock.lock();
                try{
                    this.replies.add(paraLista);
                }finally{
                    this.lock.unlock();
                }
            }

        }catch(Exception e){
            System.out.println("[ERRO CLIENTE-WORKER] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
