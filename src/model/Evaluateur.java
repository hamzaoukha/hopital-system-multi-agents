package model;

public class Evaluateur {

    public static final double W_DELAI = 1.0;
    public static final double W_FILE = 5.0;
    public static final double PENALITE_SPECIALITE = 50.0;

    public static double score(int delai, int fileAttente, boolean bonneSpecialite, int urgence) {
        double w1 = W_DELAI * (1 + urgence);
        double penalite = bonneSpecialite ? 0.0 : PENALITE_SPECIALITE;
        return w1 * delai + W_FILE * fileAttente + penalite;
    }

    public static String detail(String docteur, int delai, int file, boolean match, int urgence, double score) {
        return String.format("   %-14s delai=%3d min | file=%d | specialite=%-3s | score=%7.2f",
                docteur, delai, file, (match ? "OUI" : "NON"), score);
    }
}
