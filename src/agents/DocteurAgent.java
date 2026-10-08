package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import gui.HopitalGUI;

public class DocteurAgent extends Agent {

    public static final String TYPE_SERVICE = "Consultation";
    public static final String ONTOLOGIE = "Sante-Hopital";

    private String specialite = "Generaliste";
    private int delaiBase = 20;
    private int fileAttente = 0;
    private int maxFile = 5;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            specialite = args[0].toString();
            delaiBase = Integer.parseInt(args[1].toString());
            fileAttente = Integer.parseInt(args[2].toString());
            maxFile = Integer.parseInt(args[3].toString());
        }

        HopitalGUI.log(getLocalName(), "Deploiement du docteur - specialite : " + specialite);
        publierService();
        rafraichirGUI("Disponible");

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
        addBehaviour(parallelBehaviour);

        parallelBehaviour.addSubBehaviour(new RepondreAuCFP());
        parallelBehaviour.addSubBehaviour(new TraiterDecision());
        parallelBehaviour.addSubBehaviour(new ConsulterPatients(this, 8000));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        HopitalGUI.retirerDocteur(getLocalName());
        HopitalGUI.log(getLocalName(), "Fin de service, desinscription du DF.");
    }

    private void publierService() {
        DFAgentDescription services = new DFAgentDescription();
        services.setName(getAID());

        ServiceDescription description = new ServiceDescription();
        description.setType(TYPE_SERVICE);
        description.setName(specialite);
        services.addServices(description);

        try {
            DFService.register(this, services);
            HopitalGUI.log(getLocalName(), "Service publie dans le DF : "
                    + TYPE_SERVICE + " / " + specialite);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class RepondreAuCFP extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.CFP),
                MessageTemplate.MatchOntology(ONTOLOGIE));

        @Override
        public void action() {
            ACLMessage cfp = myAgent.receive(template);

            if (cfp != null) {
                String[] parties = cfp.getContent().split(";");
                String specialiteDemandee = parties[0];
                int urgence = Integer.parseInt(parties[1]);

                ACLMessage reponse = cfp.createReply();

                if (fileAttente >= maxFile && urgence < 4) {
                    reponse.setPerformative(ACLMessage.REFUSE);
                    reponse.setContent("Sature : " + fileAttente + " patients en attente");
                    HopitalGUI.log(getLocalName(), "REFUSE (sature) -> " + cfp.getSender().getLocalName());
                } else {
                    int delaiPropose = delaiBase + fileAttente * 5;
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(delaiPropose + ";" + fileAttente + ";" + specialite);
                    HopitalGUI.log(getLocalName(), "PROPOSE delai=" + delaiPropose
                            + "min, file=" + fileAttente
                            + " (demande : " + specialiteDemandee + ")");
                }

                reponse.setLanguage("Fr");
                reponse.setOntology(ONTOLOGIE);
                myAgent.send(reponse);
            } else {
                block();
            }
        }
    }

    private class TraiterDecision extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                        MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)),
                MessageTemplate.MatchOntology(ONTOLOGIE));

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(template);

            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    fileAttente++;
                    int delai = delaiBase + fileAttente * 5;

                    ACLMessage confirmation = msg.createReply();
                    confirmation.setPerformative(ACLMessage.INFORM);
                    confirmation.setOntology(ONTOLOGIE);
                    confirmation.setLanguage("Fr");
                    confirmation.setContent(getLocalName() + ";" + specialite + ";" + delai);
                    myAgent.send(confirmation);

                    HopitalGUI.log(getLocalName(), "ACCEPT recu -> rendez-vous confirme pour "
                            + msg.getContent() + " (file = " + fileAttente + ")");
                    rafraichirGUI(fileAttente >= maxFile ? "Sature" : "Occupe");
                } else {
                    HopitalGUI.log(getLocalName(), "REJECT recu, un confrere a ete choisi.");
                }
            } else {
                block();
            }
        }
    }

    private class ConsulterPatients extends TickerBehaviour {

        public ConsulterPatients(Agent a, long periode) {
            super(a, periode);
        }

        @Override
        protected void onTick() {
            if (fileAttente > 0) {
                fileAttente--;
                HopitalGUI.log(getLocalName(), "Consultation terminee, file = " + fileAttente);
            }
            rafraichirGUI(fileAttente == 0 ? "Disponible"
                    : (fileAttente >= maxFile ? "Sature" : "Occupe"));
        }
    }

    private void rafraichirGUI(String etat) {
        HopitalGUI.majDocteur(getLocalName(), specialite, fileAttente,
                delaiBase + fileAttente * 5, etat);
    }
}
