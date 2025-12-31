package utils.workers.server;

import main.Server;

public class ServerSimulator implements Runnable{
    private final Server server;
    private final long intervalo;

    public ServerSimulator(Server server, long intervalo) {
        this.server = server;
        this.intervalo = intervalo;
    }

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
