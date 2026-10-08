package containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class PatientContainer {

    public static void main(String[] args) throws ControllerException, InterruptedException {

        Runtime runtime = Runtime.instance();

        ProfileImpl config = new ProfileImpl();
        config.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        config.setParameter(Profile.CONTAINER_NAME, "Container-Patients");
        AgentContainer conteneur = runtime.createAgentContainer(config);
        conteneur.start();

        String[][] patients = {
                {"Patient_Youssef", "douleur thoracique"},
                {"Patient_Salma", "fracture du bras"},
                {"Patient_Karim", "migraine persistante"}
        };

        for (String[] p : patients) {
            conteneur.createNewAgent(p[0], "agents.PatientAgent",
                    new Object[]{p[1]}).start();
            System.out.println("Patient arrive : " + p[0] + " (" + p[1] + ")");
            Thread.sleep(5000);
        }
    }
}
