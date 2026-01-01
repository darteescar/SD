package entities;

import entities.payloads.Evento;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Serie {
    private String data;
    private List<Evento> eventos;
    public Serie(String data){
        this.eventos = new ArrayList<>();
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public void add(Evento evento){
            this.eventos.add(evento);
    }

    public int size(){
            return this.eventos.size();
    }

    @Override
    public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(this.data).append(":\n");
            for(Evento e : this.eventos){
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
    }

    public void serialize(DataOutputStream dos) {
        try{

            dos.writeInt(this.eventos.size());
            for(Evento e : this.eventos){
                byte[] eventoBytes = e.serialize();
                dos.writeInt(eventoBytes.length);
                dos.write(eventoBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Evento> getEventos() {
            return new ArrayList<>(this.eventos);
    }

    public int calcQuantidadeVendas(String produto) {
        int total = 0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade();
            }
        }
        return total;
    }

    public double calcVolumeVendas(String produto) {
        double total = 0.0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade() * evento.getPreco();
            }
        }
        return total;
    }

    public double calcPrecoMaximo(String produto) {
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