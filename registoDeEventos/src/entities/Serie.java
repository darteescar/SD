package entities;

import entities.payloads.Evento;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Representa uma série de eventos ocorridos em uma data específica */
public class Serie {

    /** Data da série de eventos */
    private String data;

    /** Lista de eventos na série */
    private List<Evento> eventos;

    /** 
     * Construtor da classe Serie
     * 
     * @param data Data da série de eventos
     * @return Uma nova instância de Serie
     */
    public Serie(String data){
        this.eventos = new ArrayList<>();
        this.data = data;
    }

    /** 
     * Obtém a data da série de eventos
     * 
     * @return Data da série de eventos
     */
    public String getData() {
        return this.data;
    }

    /** 
     * Adiciona um evento à série de eventos
     * 
     * @param evento Evento a ser adicionado
     */
    public void add(Evento evento){
            this.eventos.add(evento);
    }

    /** 
     * Obtém o número de eventos na série
     * 
     * @return Número de eventos na série
     */
    public int size(){
            return this.eventos.size();
    }

    /** 
     * Representação em string da série de eventos
     * 
     * @return String representando a série de eventos
     */
    @Override
    public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(this.data).append(":\n");
            for(Evento e : this.eventos){
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
    }

    /** 
     * Serializa a série de eventos em um DataOutputStream
     * 
     * @param dos DataOutputStream onde a série será serializada
     */
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

    /** 
     * Obtém uma cópia da lista de eventos na série
     * 
     * @return Cópia da lista de eventos na série
     */
    public List<Evento> getEventos() {
            return new ArrayList<>(this.eventos);
    }

    /** 
     * Calcula a quantidade total de vendas para um produto específico
     * 
     * @param produto Nome do produto
     * @return Quantidade total de vendas do produto
     */
    public int calcQuantidadeVendas(String produto) {
        int total = 0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade();
            }
        }
        return total;
    }

    /** 
     * Calcula o volume total de vendas para um produto específico
     * 
     * @param produto Nome do produto
     * @return Volume total de vendas do produto
     */
    public double calcVolumeVendas(String produto) {
        double total = 0.0;
        for (Evento evento : this.eventos) {
            if (evento.getProduto().equals(produto)) {
                total += evento.getQuantidade() * evento.getPreco();
            }
        }
        return total;
    }

    /** 
     * Calcula o preço máximo registrado para um produto específico
     * 
     * @param produto Nome do produto
     * @return Preço máximo do produto
     */
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

    /** 
     * Filtra os eventos da série com base em uma lista de produtos
     * 
     * @param produtos Lista de produtos para filtrar
     * @return Lista de eventos que correspondem aos produtos fornecidos
     */
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