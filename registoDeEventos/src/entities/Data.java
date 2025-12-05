package entities;

public class Data {
    private int dia;
    private int mes;
    private int ano;

    public Data(int dia, int mes, int ano){
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
    }

    public String getData(){
        return dia + "/" +  mes + "/" + ano;
    }

    public void incrementData(){
        if(ultimoDiaMes() && mes == 12){
            this.dia = 1;
            this.mes = 1;
            this.ano++;
        }
        else if(ultimoDiaMes()){
            this.dia = 1;
            this.mes++;
        }
        else this.dia++;
    }

    public int compareData(String data){
        Data d = stringToData(data); 
        if(this.ano < d.ano) return -1;
        else if(this.ano > d.ano) return 1;
        else{
            if(this.mes < d.mes) return -1;
            else if(this.mes > d.mes) return 1;
            else{
                if(this.dia < d.dia) return -1;
                else if(this.dia > d.dia) return 1;
                else return 0;
            }
        }
    }

    private Data stringToData(String data){
        String[] pedaco = data.split("/");
        int dia = Integer.parseInt(pedaco[0]);
        int mes = Integer.parseInt(pedaco[1]);
        int ano = Integer.parseInt(pedaco[2]);
        return new Data(dia, mes, ano);
    }

    private boolean mes31(){
        if(this.mes == 1
        || this.mes == 3
        || this.mes == 5
        || this.mes == 7
        || this.mes == 8
        || this.mes == 10
        || this.mes == 12
        ) return true;
        else return false;
    }

    private boolean mes30(){
        if(this.mes == 4
        || this.mes == 6
        || this.mes == 9
        || this.mes == 11
        ) return true;
        else return false;
    }

    private boolean fevereiro(){
        return this.mes == 2;
    }

    private boolean anoBi(){
        return ano % 4 == 0;
    }

    private boolean ultimoDiaMes(){
        if(anoBi() && fevereiro() && dia == 29) return true;
        else if (!anoBi() && fevereiro() && dia == 28) return true;
        else if(mes31() && dia == 31) return true;
        else if(mes30() && dia == 30) return true;
        else return false;
    }
}
