package containers;

import java.util.List;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import gui.HopitalGUI;
import model.Docteur;
import util.Persistance;

public class LancerTout {

    public static void main(String[] args) throws ControllerException, InterruptedException {

        Runtime runtime = Runtime.instance();
        runtime.setCloseVM(true);

        ProfileImpl profilPrincipal = new ProfileImpl();
        profilPrincipal.setParameter(ProfileImpl.GUI, "true");
        AgentContainer mainContainer = runtime.createMainContainer(profilPrincipal);
        mainContainer.start();
        System.out.println("MainContainer demarre (AMS + DF)");
        Thread.sleep(1500);

        AgentContainer conteneurDocteurs = creerConteneur(runtime, "Container-Docteurs");
        List<Docteur> docteurs = Persistance.chargerDocteurs();
        for (Docteur d : docteurs) {
            Object[] arguments = new Object[]{
                    d.getSpecialite(),
                    String.valueOf(d.getDelaiBase()),
                    String.valueOf(d.getFileAttente()),
                    String.valueOf(d.getMaxFile())
            };
            AgentController ac = conteneurDocteurs.createNewAgent(
                    d.getNom(), "agents.DocteurAgent", arguments);
            ac.start();
        }
        System.out.println(docteurs.size() + " docteurs crees");

        AgentContainer conteneurInfirmier = creerConteneur(runtime, "Container-Infirmier");
        AgentController infirmier = conteneurInfirmier.createNewAgent(
                "Infirmier1", "agents.InfirmierAgent", null);
        infirmier.start();

        final AgentContainer conteneurPatients = creerConteneur(runtime, "Container-Patients");

        HopitalGUI.demarrer(new HopitalGUI.CreateurPatient() {
            public void creerPatient(String nom, String symptome) {
                try {
                    AgentController ac = conteneurPatients.createNewAgent(
                            nom, "agents.PatientAgent", new Object[]{symptome});
                    ac.start();
                } catch (ControllerException e) {
                    HopitalGUI.log("GUI", "Impossible de creer le patient : " + e.getMessage());
                }
            }
        });

        Thread.sleep(1500);

        conteneurPatients.createNewAgent("Patient_Youssef", "agents.PatientAgent",
                new Object[]{"douleur thoracique"}).start();
        Thread.sleep(4000);
        conteneurPatients.createNewAgent("Patient_Salma", "agents.PatientAgent",
                new Object[]{"fracture du bras"}).start();

        System.out.println("Systeme pret.");
    }

    private static AgentContainer creerConteneur(Runtime runtime, String nom)
            throws ControllerException {
        ProfileImpl profil = new ProfileImpl();
        profil.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        profil.setParameter(Profile.CONTAINER_NAME, nom);
        AgentContainer conteneur = runtime.createAgentContainer(profil);
        conteneur.start();
        return conteneur;
    }
}
