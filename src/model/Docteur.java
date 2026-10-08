package model;

public class Docteur {

    private String nom;
    private String specialite;
    private int delaiBase;
    private int fileAttente;
    private int maxFile;

    public Docteur(String nom, String specialite, int delaiBase, int fileAttente, int maxFile) {
        this.nom = nom;
        this.specialite = specialite;
        this.delaiBase = delaiBase;
        this.fileAttente = fileAttente;
        this.maxFile = maxFile;
    }

    public String getNom() {
        return nom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public int getDelaiBase() {
        return delaiBase;
    }

    public int getFileAttente() {
        return fileAttente;
    }

    public int getMaxFile() {
        return maxFile;
    }

    @Override
    public String toString() {
        return nom + " (" + specialite + ", delai=" + delaiBase + "min, file=" + fileAttente + ")";
    }
}
