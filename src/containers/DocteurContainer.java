package containers;

import java.util.List;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import model.Docteur;
import util.Persistance;

public class DocteurContainer {

    public static void main(String[] args) throws ControllerException {

        Runtime runtime = Runtime.instance();

        ProfileImpl config = new ProfileImpl();
        config.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        config.setParameter(Profile.CONTAINER_NAME, "Container-Docteurs");
        AgentContainer conteneur = runtime.createAgentContainer(config);
        conteneur.start();

        List<Docteur> docteurs = Persistance.chargerDocteurs();
        for (Docteur d : docteurs) {
            Object[] arguments = new Object[]{
                    d.getSpecialite(),
                    String.valueOf(d.getDelaiBase()),
                    String.valueOf(d.getFileAttente()),
                    String.valueOf(d.getMaxFile())
            };
            AgentController controller = conteneur.createNewAgent(
                    d.getNom(), "agents.DocteurAgent", arguments);
            controller.start();
            System.out.println("Docteur demarre : " + d);
        }
    }
}
