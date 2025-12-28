package entities;

import entities.payloads.Evento;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Serie {
    private String data;
    private List<Evento> eventos;
    private ReentrantLock lock;

    public Serie(String data){
        this.eventos = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public void add(Evento evento){
        this.lock.lock();
        try{
            this.eventos.add(evento);
        }finally{
            this.lock.unlock();
        }
    }

    public int size(){
        this.lock.lock();
        try{
            return this.eventos.size();
        }finally{
            this.lock.unlock();
        }
    }

    @Override
    public String toString(){
        this.lock.lock();
        try{
            StringBuilder sb = new StringBuilder();
            sb.append(this.data).append(":\n");
            for(Evento e : this.eventos){
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
        }finally{
            this.lock.unlock();
        }
    }

    public void serialize(DataOutputStream dos) {
        this.lock.lock();
        try{

            dos.writeInt(this.eventos.size());
            for(Evento e : this.eventos){
                byte[] eventoBytes = e.serialize();
                dos.writeInt(eventoBytes.length);
                dos.write(eventoBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            this.lock.unlock();
        }
    }

    /*public static Serie deserialize(DataInputStream dis) throws IOException{
        Serie serie = new Serie();
        int size = dis.readInt();
        for(int i = 0; i < size; i++){
            int eventoSize = dis.readInt();
            byte[] eventoBytes = new byte[eventoSize];
            dis.readFully(eventoBytes);
            Evento evento = Evento.deserialize(eventoBytes);
            serie.add(evento);
        }
        return serie;
    }*/

    public List<Evento> getEventos() {
        this.lock.lock();
        try {
            return new ArrayList<>(this.eventos);
        } finally {
            this.lock.unlock();
        }
    }

    public int calcQuantidadeVendas(String produto) { // precisa de lock ???
        int total = 0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade();
            }
        }
        return total;
    }

    public double calcVolumeVendas(String produto) { // precisa de lock ???
        double total = 0.0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade() * evento.getPreco();
            }
        }
        return total;
    }

    public double calcPrecoMaximo(String produto) { // precisa de lock ???
        double maxPreco = 0.0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                double preco = evento.getPreco();
                if (preco > maxPreco) {
                    maxPreco = preco;
                }
            }
        }
        return maxPreco;
    }

    public List<Evento> filtrarEventos(List<String> produtos) {
        List<Evento> filtrados = new ArrayList<>();
        for (Evento evento : this.eventos) {
            if (produtos.contains(evento.getProduto())) {
                filtrados.add(evento);
            }
        }
        return filtrados;
    }
}