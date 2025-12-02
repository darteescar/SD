package entities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Serie {
    private List<Evento> eventos;
    private ReentrantLock lock;

    public Serie(){
        this.eventos = new ArrayList<>();
        this.lock = new ReentrantLock();
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
            for(Evento e : this.eventos){
                sb.append(e.toString()).append("\n");
            }
            return sb.toString();
        }finally{
            this.lock.unlock();
        }
    }

    public void serialize(DataOutputStream dos) throws IOException{
        this.lock.lock();
        try{
            dos.writeInt(this.size());
            for(Evento e : this.eventos){
                e.serialize(dos);
            }
        }finally{
            this.lock.unlock();
        }
    }

    public static Serie deserialize(DataInputStream dis) throws IOException{
        Serie serie = new Serie();

        int size = dis.readInt();
        for(int i = 0; i < size; i++){
            Evento evento = Evento.deserialize(dis);
            serie.add(evento);
        }
        return serie;
    }
}
