package web.server;

import java.util.Vector;

public class root {
    public int annoDiInizio;
    public aula aula = new aula();
    public String classe;
    public int numeroFinestre;
    public String specializzazione;
    public Vector <element> studenti = new Vector<>();
    
    public int getAnnoDiInizio() {
        return annoDiInizio;
    }
    public void setAnnoDiInizio(int annoDiInizio) {
        this.annoDiInizio = annoDiInizio;
    }
    public aula getAula() {
        return aula;
    }
    public void setAula(aula aula) {
        this.aula = aula;
    }
    public String getClasse() {
        return classe;
    }
    public void setClasse(String classe) {
        this.classe = classe;
    }
    public int getNumeroFinestre() {
        return numeroFinestre;
    }
    public void setNumeroFinestre(int numeroFinestreM) {
        this.numeroFinestre = numeroFinestreM;
    }
    public String getSpecializzazione() {
        return specializzazione;
    }
    public void setSpecializzazione(String specializzazione) {
        this.specializzazione = specializzazione;
    }
    public Vector<element> getStudenti() {
        return studenti;
    }
    public void setStudenti(Vector<element> studenti) {
        this.studenti = studenti;
    }
}
