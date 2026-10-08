package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import gui.HopitalGUI;

public class PatientAgent extends Agent {

    public static final String ONTOLOGIE = "Sante-Hopital";

    private String symptome = "douleur";

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            symptome = args[0].toString();
        }

        HopitalGUI.log(getLocalName(), "Arrivee aux urgences. Symptome : " + symptome);

        addBehaviour(new WakerBehaviour(this, 2000) {
            @Override
            protected void onWake() {
                AID infirmier = chercherInfirmier();
                if (infirmier == null) {
                    HopitalGUI.log(getLocalName(), "Aucun infirmier disponible, je repars.");
                    doDelete();
                    return;
                }
                ACLMessage requete = new ACLMessage(ACLMessage.REQUEST);
                requete.addReceiver(infirmier);
                requete.setContent(symptome);
                requete.setLanguage("Fr");
                requete.setOntology(ONTOLOGIE);
                send(requete);
                HopitalGUI.log(getLocalName(), "REQUEST envoye a " + infirmier.getLocalName());
            }
        });

        addBehaviour(new AttendreReponse());
    }

    @Override
    protected void takeDown() {
        HopitalGUI.log(getLocalName(), "Je quitte l'hopital (agent termine).");
    }

    private AID chercherInfirmier() {
        DFAgentDescription modele = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(InfirmierAgent.TYPE_SERVICE);
        modele.addServices(sd);
        try {
            DFAgentDescription[] resultat = DFService.search(this, modele);
            if (resultat.length > 0) {
                return resultat[0].getName();
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class AttendreReponse extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchOntology(ONTOLOGIE),
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)));

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(template);

            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    HopitalGUI.log(getLocalName(), "INFORM recu -> " + msg.getContent());
                } else {
                    HopitalGUI.log(getLocalName(), "FAILURE recu -> " + msg.getContent());
                }
                myAgent.doDelete();
            } else {
                block();
            }
        }
    }
}
