package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Consultation;
import model.Docteur;

public class Persistance {

    public static final String FICHIER_DOCTEURS = "data/docteurs.json";
    public static final String FICHIER_CONSULTATIONS = "data/consultations.json";

    public static List<Docteur> chargerDocteurs() {
        List<Docteur> docteurs = new ArrayList<Docteur>();
        try {
            List<Map<String, String>> lignes = SimpleJson.lireTableau(FICHIER_DOCTEURS);
            for (Map<String, String> m : lignes) {
                docteurs.add(new Docteur(
                        m.get("nom"),
                        m.get("specialite"),
                        SimpleJson.entier(m, "delaiBase", 20),
                        SimpleJson.entier(m, "fileAttente", 0),
                        SimpleJson.entier(m, "maxFile", 5)));
            }
        } catch (Exception e) {
            System.out.println("[Persistance] Lecture de " + FICHIER_DOCTEURS
                    + " impossible (" + e.getMessage() + "), utilisation des docteurs par defaut.");
        }

        if (docteurs.isEmpty()) {
            docteurs.add(new Docteur("Dr_Alami", "Cardiologie", 10, 2, 5));
            docteurs.add(new Docteur("Dr_Bennis", "Cardiologie", 25, 0, 5));
            docteurs.add(new Docteur("Dr_Chafik", "Generaliste", 5, 4, 6));
            docteurs.add(new Docteur("Dr_Douiri", "Traumatologie", 15, 1, 4));
        }
        return docteurs;
    }

    public static synchronized void enregistrerConsultation(Consultation c) {
        try {
            List<Map<String, String>> lignes;
            File f = new File(FICHIER_CONSULTATIONS);
            if (f.exists()) {
                lignes = SimpleJson.lireTableau(FICHIER_CONSULTATIONS);
            } else {
                lignes = new ArrayList<Map<String, String>>();
                if (f.getParentFile() != null) {
                    f.getParentFile().mkdirs();
                }
            }
            lignes.add(c.versMap());
            SimpleJson.ecrireTableau(FICHIER_CONSULTATIONS, lignes);
            System.out.println("[Persistance] Consultation enregistree pour " + c.getPatient()
                    + " (total : " + lignes.size() + ")");
        } catch (Exception e) {
            System.out.println("[Persistance] Echec de l'enregistrement : " + e.getMessage());
        }
    }
}
