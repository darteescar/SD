package entities;

/** Data de uma Série */
public class Data {

    /** Dia */
    private int dia;

    /** Mês */
    private int mes;

    /** Ano */
    private int ano;

    /** 
     * Construtor de Data
     * 
     * @param dia Dia
     * @param mes Mês
     * @param ano Ano
     * @return A nova Data
     */
    public Data(int dia, int mes, int ano){
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
    }

    /** 
     * Devolve o dia
     * 
     * @return Dia
     */
    public int getDia(){
        return this.dia;
    }

    /** 
     * Devolve o mês
     * 
     * @return Mês
     */
    public int getMes(){
        return this.mes;
    }

    /** 
     * Devolve o ano
     * 
     * @return Ano
     */
    public int getAno(){
        return this.ano;
    }

    /** 
     * Devolve a data em formato String
     * 
     * @return Data em formato String
     */
    public String getData(){
        return dia + "/" +  mes + "/" + ano;
    }

    /** 
     * Incrementa a data em um dia
     */
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

    /** 
     * Decrementa a data em um dia
     */
    public void decrementData(){
        if(this.dia == 1 && this.mes == 1){
            this.dia = 31;
            this.mes = 12;
            this.ano--;
        }
        else if(this.dia == 1){
            this.mes--;
            if(mes31()) this.dia = 31;
            else if(mes30()) this.dia = 30;
            else if(fevereiro()){
                if(anoBi()) this.dia = 29;
                else this.dia = 28;
            }
        }
        else this.dia--;
    }

    /** 
     * Compara a data com outra data em formato String
     * 
     * @param data Data em formato String
     * @return -1 se a data for menor, 1 se for maior, 0 se forem iguais
     */
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

    /** 
     * Converte uma String em um objeto Data 
     * 
     * @param data Data em formato String
     * @return Data
    */
    private Data stringToData(String data){
        String[] pedaco = data.split("/");
        int dia = Integer.parseInt(pedaco[0]);
        int mes = Integer.parseInt(pedaco[1]);
        int ano = Integer.parseInt(pedaco[2]);
        return new Data(dia, mes, ano);
    }

    /** 
     * Verifica se o mês tem 31 dias
     * 
     * @return true se o mês tiver 31 dias, false caso contrário
     */
    private boolean mes31(){
        return this.mes == 1
                || this.mes == 3
                || this.mes == 5
                || this.mes == 7
                || this.mes == 8
                || this.mes == 10
                || this.mes == 12;
    }

    /** 
     * Verifica se o mês tem 30 dias
     * 
     * @return true se o mês tiver 30 dias, false caso contrário
     */
    private boolean mes30(){
        return this.mes == 4
                || this.mes == 6
                || this.mes == 9
                || this.mes == 11;
    }


    /** 
     * Verifica se o mês é fevereiro
     * 
     * @return true se o mês for fevereiro, false caso contrário
     */
    private boolean fevereiro(){
        return this.mes == 2;
    }

    /** 
     * Verifica se o ano é bissexto
     * 
     * @return true se o ano for bissexto, false caso contrário
     */
    private boolean anoBi(){
        return ano % 4 == 0;
    }

    /** 
     * Verifica se é o último dia do mês
     * 
     * @return true se for o último dia do mês, false caso contrário
     */
    private boolean ultimoDiaMes(){
        if(anoBi() && fevereiro() && dia == 29) return true;
        else if (!anoBi() && fevereiro() && dia == 28) return true;
        else if(mes31() && dia == 31) return true;
        else return mes30() && dia == 30;
    }

    /** 
     * Devolve a data em formato String
     * 
     * @return Data em formato String
     */
    @Override
    public String toString(){
        return this.dia + "/" + this.mes + "/" + this.ano;
    }

    /** 
     * Clona o objeto Data
     * 
     * @return Cópia do objeto Data
     */
    @Override
    public Data clone(){
        return new Data(this.dia, this.mes, this.ano);
    }
}
