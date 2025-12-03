package structs;

public class Par<X,Y> {
    private X x;
    private Y y;

    public Par(X x, Y y){
        this.x = x;
        this.y = y;
    }
    public X fst(){
        return this.x;
    }

    public Y snd(){
        return this.y;
    }
}
