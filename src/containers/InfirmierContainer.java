package containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import gui.HopitalGUI;

public class InfirmierContainer {

    public static void main(String[] args) throws ControllerException {

        Runtime runtime = Runtime.instance();

        ProfileImpl config = new ProfileImpl();
        config.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        config.setParameter(Profile.CONTAINER_NAME, "Container-Infirmier");
        final AgentContainer conteneur = runtime.createAgentContainer(config);
        conteneur.start();

        AgentController infirmier = conteneur.createNewAgent(
                "Infirmier1", "agents.InfirmierAgent", null);
        infirmier.start();

        HopitalGUI.demarrer(new HopitalGUI.CreateurPatient() {
            public void creerPatient(String nom, String symptome) {
                try {
                    conteneur.createNewAgent(nom, "agents.PatientAgent",
                            new Object[]{symptome}).start();
                } catch (ControllerException e) {
                    HopitalGUI.log("GUI", "Creation impossible : " + e.getMessage());
                }
            }
        });
    }
}
