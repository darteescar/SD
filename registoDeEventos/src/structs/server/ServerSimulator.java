package structs.server;

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
                Thread.sleep(30000); // Simula a passagem de um dia a cada 10 segundos
                server.passarDia();
                server.printGS();
                System.out.println("Dia passado no servidor.");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
