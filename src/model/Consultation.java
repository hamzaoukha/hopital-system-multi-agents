package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Consultation {

    private String id;
    private String patient;
    private String symptome;
    private String specialite;
    private int urgence;
    private String docteur;
    private int delai;
    private String date;

    public Consultation(String id, String patient, String symptome, String specialite,
                        int urgence, String docteur, int delai) {
        this.id = id;
        this.patient = patient;
        this.symptome = symptome;
        this.specialite = specialite;
        this.urgence = urgence;
        this.docteur = docteur;
        this.delai = delai;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public Map<String, String> versMap() {
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("id", id);
        m.put("patient", patient);
        m.put("symptome", symptome);
        m.put("specialite", specialite);
        m.put("urgence", String.valueOf(urgence));
        m.put("docteur", docteur);
        m.put("delai", String.valueOf(delai));
        m.put("date", date);
        return m;
    }

    public String getDocteur() {
        return docteur;
    }

    public int getDelai() {
        return delai;
    }

    public String getPatient() {
        return patient;
    }
}
