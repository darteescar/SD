package utils.workers.server;

import main.Server;

/** Thread responsável por simular a passagem do tempo no servidor */
public class ServerSimulator implements Runnable{

    /** Server */
    private final Server server;

    /** Intervalo de tempo em milissegundos para simular a passagem de um dia */
    private final long intervalo;

    /** 
     * Construtor da classe ServerSimulator
     * 
     * @param server Server
     * @param intervalo Intervalo de tempo em milissegundos para simular a passagem de um dia
     * @return Uma nova instância de ServerSimulator
     */
    public ServerSimulator(Server server, long intervalo) {
        this.server = server;
        this.intervalo = intervalo;
    }

    /** 
     * Método run da thread que simula a passagem do tempo no servidor
     */
    @Override
    public void run(){
        try{
            while (true) {
                Thread.sleep(intervalo); // Simula a passagem de um dia a cada intervalo
                server.passarDia();
                //server.printGS();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
