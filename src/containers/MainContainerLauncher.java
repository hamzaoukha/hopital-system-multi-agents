package containers;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class MainContainerLauncher {

    public static void main(String[] args) throws ControllerException {

        Runtime runtime = Runtime.instance();

        ProfileImpl profileImpl = new ProfileImpl();
        profileImpl.setParameter(ProfileImpl.GUI, "true");
        AgentContainer mainAgentContainer = runtime.createMainContainer(profileImpl);

        mainAgentContainer.start();

        System.out.println("MainContainer pret : AMS et DF sont actifs.");
    }
}
