package utils.workers.server;

import main.Server;

public class ServerSimulator implements Runnable{
    private final Server server;

    public ServerSimulator(Server server) {
        this.server = server;
    }

    @Override
    public void run(){
        try{
            while (true) {
                Thread.sleep(1000000); // Simula a passagem de um dia a cada 1000 segundos
                server.passarDia();
                //server.printGS();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
