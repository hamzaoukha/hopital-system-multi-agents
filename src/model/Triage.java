package model;

public class Triage {

    public static int niveauUrgence(String symptome) {
        String s = symptome.toLowerCase();

        if (contient(s, "arret", "hemorragie", "inconscient", "detresse", "avc")) {
            return 5;
        }
        if (contient(s, "douleur thoracique", "thoracique", "essoufflement", "convulsion", "fracture ouverte")) {
            return 4;
        }
        if (contient(s, "fracture", "brulure", "vomissement", "forte fievre", "palpitation")) {
            return 3;
        }
        if (contient(s, "fievre", "douleur", "vertige", "migraine", "eruption")) {
            return 2;
        }
        return 1;
    }

    public static String specialiteRequise(String symptome) {
        String s = symptome.toLowerCase();

        if (contient(s, "thoracique", "palpitation", "coeur", "tension", "arret")) {
            return "Cardiologie";
        }
        if (contient(s, "fracture", "entorse", "genou", "epaule", "dos")) {
            return "Traumatologie";
        }
        if (contient(s, "migraine", "vertige", "convulsion", "avc", "engourdissement")) {
            return "Neurologie";
        }
        if (contient(s, "eruption", "bouton", "demangeaison", "brulure")) {
            return "Dermatologie";
        }
        if (contient(s, "toux", "essoufflement", "asthme", "respiration")) {
            return "Pneumologie";
        }
        return "Generaliste";
    }

    public static String libelle(int urgence) {
        switch (urgence) {
            case 5: return "CRITIQUE";
            case 4: return "TRES URGENT";
            case 3: return "URGENT";
            case 2: return "MODERE";
            default: return "FAIBLE";
        }
    }

    private static boolean contient(String source, String... motsCles) {
        for (String mot : motsCles) {
            if (source.contains(mot)) {
                return true;
            }
        }
        return false;
    }
}
