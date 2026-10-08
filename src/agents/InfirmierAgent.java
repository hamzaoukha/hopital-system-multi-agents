package agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import gui.HopitalGUI;
import model.Consultation;
import model.Evaluateur;
import model.Triage;
import util.Persistance;

public class InfirmierAgent extends Agent {

    public static final String TYPE_SERVICE = "Triage";
    public static final String ONTOLOGIE = "Sante-Hopital";

    private static final long DELAI_REPONSE = 3000;

    private int compteurDossiers = 0;

    @Override
    protected void setup() {
        HopitalGUI.log(getLocalName(), "Poste de triage ouvert.");
        publierService();
        addBehaviour(new EcouterPatients());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        HopitalGUI.log(getLocalName(), "Poste de triage ferme.");
    }

    private void publierService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TYPE_SERVICE);
        sd.setName("Accueil-Urgences");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class EcouterPatients extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology(ONTOLOGIE));

        @Override
        public void action() {
            ACLMessage requete = myAgent.receive(template);

            if (requete != null) {
                String symptome = requete.getContent();
                int urgence = Triage.niveauUrgence(symptome);
                String specialite = Triage.specialiteRequise(symptome);
                compteurDossiers++;
                String dossier = "DOS-" + String.format("%03d", compteurDossiers);

                HopitalGUI.log(getLocalName(), "REQUEST de "
                        + requete.getSender().getLocalName() + " : \"" + symptome + "\"");
                HopitalGUI.log(getLocalName(), "Triage " + dossier + " -> urgence "
                        + urgence + " (" + Triage.libelle(urgence) + "), specialite : " + specialite);

                myAgent.addBehaviour(new NegocierConsultation(
                        requete.getSender(), dossier, symptome, specialite, urgence));
            } else {
                block();
            }
        }
    }

    private class NegocierConsultation extends Behaviour {

        private final AID patient;
        private final String dossier;
        private final String symptome;
        private final String specialite;
        private final int urgence;

        private int etape = 0;
        private long echeance;
        private int reponsesAttendues = 0;
        private int reponsesRecues = 0;

        private final List<ACLMessage> propositions = new ArrayList<ACLMessage>();
        private ACLMessage meilleure = null;

        private MessageTemplate tmplPropositions;
        private MessageTemplate tmplConfirmation;

        public NegocierConsultation(AID patient, String dossier, String symptome,
                                    String specialite, int urgence) {
            this.patient = patient;
            this.dossier = dossier;
            this.symptome = symptome;
            this.specialite = specialite;
            this.urgence = urgence;
        }

        @Override
        public void action() {
            switch (etape) {
                case 0: envoyerCFP();           break;
                case 1: collecterReponses();    break;
                case 2: choisirMeilleure();     break;
                case 3: attendreConfirmation(); break;
                default: break;
            }
        }

        @Override
        public boolean done() {
            return etape == 4;
        }

        private void envoyerCFP() {
            AID[] docteurs = chercherDocteurs();

            if (docteurs.length == 0) {
                HopitalGUI.log(getLocalName(), "Aucun docteur trouve dans le DF !");
                repondreAuPatient(ACLMessage.FAILURE, "Aucun docteur disponible pour le moment.");
                etape = 4;
                return;
            }

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (AID d : docteurs) {
                cfp.addReceiver(d);
            }
            cfp.setContent(specialite + ";" + urgence);
            cfp.setLanguage("Fr");
            cfp.setOntology(ONTOLOGIE);
            cfp.setConversationId(dossier);
            cfp.setReplyWith(dossier + "-cfp");
            myAgent.send(cfp);

            reponsesAttendues = docteurs.length;
            echeance = System.currentTimeMillis() + DELAI_REPONSE;

            tmplPropositions = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(dossier),
                    MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE)));

            HopitalGUI.log(getLocalName(), "CFP " + dossier + " envoye a "
                    + reponsesAttendues + " docteur(s).");
            etape = 1;
        }

        private AID[] chercherDocteurs() {
            DFAgentDescription modele = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DocteurAgent.TYPE_SERVICE);
            modele.addServices(sd);
            try {
                DFAgentDescription[] resultat = DFService.search(myAgent, modele);
                AID[] aids = new AID[resultat.length];
                for (int i = 0; i < resultat.length; i++) {
                    aids[i] = resultat[i].getName();
                }
                return aids;
            } catch (FIPAException e) {
                e.printStackTrace();
                return new AID[0];
            }
        }

        private void collecterReponses() {
            ACLMessage reponse = myAgent.receive(tmplPropositions);

            if (reponse != null) {
                reponsesRecues++;
                if (reponse.getPerformative() == ACLMessage.PROPOSE) {
                    propositions.add(reponse);
                } else {
                    HopitalGUI.log(getLocalName(), "REFUSE de "
                            + reponse.getSender().getLocalName());
                }
            } else {
                block(200);
            }

            if (reponsesRecues >= reponsesAttendues || System.currentTimeMillis() > echeance) {
                etape = 2;
            }
        }

        private void choisirMeilleure() {
            if (propositions.isEmpty()) {
                HopitalGUI.log(getLocalName(), "Aucune proposition recue pour " + dossier);
                repondreAuPatient(ACLMessage.FAILURE, "Tous les docteurs sont satures.");
                etape = 4;
                return;
            }

            HopitalGUI.log(getLocalName(), "Evaluation des propositions (" + dossier + ") :");
            double meilleurScore = Double.MAX_VALUE;

            for (ACLMessage p : propositions) {
                String[] parties = p.getContent().split(";");
                int delai = Integer.parseInt(parties[0]);
                int file = Integer.parseInt(parties[1]);
                boolean match = specialite.equalsIgnoreCase(parties[2]);

                double score = Evaluateur.score(delai, file, match, urgence);
                HopitalGUI.log("  evaluation", Evaluateur.detail(
                        p.getSender().getLocalName(), delai, file, match, urgence, score));

                if (score < meilleurScore) {
                    meilleurScore = score;
                    meilleure = p;
                }
            }

            for (ACLMessage p : propositions) {
                ACLMessage decision = p.createReply();
                decision.setOntology(ONTOLOGIE);
                if (p == meilleure) {
                    decision.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    decision.setContent(patient.getLocalName());
                } else {
                    decision.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    decision.setContent("Proposition non retenue");
                }
                myAgent.send(decision);
            }

            HopitalGUI.log(getLocalName(), "BEST PROP = "
                    + meilleure.getSender().getLocalName()
                    + " (score " + String.format("%.2f", meilleurScore) + ") -> ACCEPT_PROPOSAL");

            tmplConfirmation = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(dossier),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            echeance = System.currentTimeMillis() + DELAI_REPONSE;
            etape = 3;
        }

        private void attendreConfirmation() {
            ACLMessage confirmation = myAgent.receive(tmplConfirmation);

            if (confirmation != null) {
                String[] parties = confirmation.getContent().split(";");
                String docteur = parties[0];
                int delai = Integer.parseInt(parties[2]);

                Consultation c = new Consultation(dossier, patient.getLocalName(), symptome,
                        specialite, urgence, docteur, delai);
                Persistance.enregistrerConsultation(c);
                HopitalGUI.consultationConfirmee();

                repondreAuPatient(ACLMessage.INFORM,
                        "Dossier " + dossier + " : rendez-vous avec " + docteur
                                + " (" + parties[1] + ") dans " + delai + " minutes. Urgence "
                                + Triage.libelle(urgence) + ".");
                etape = 4;
            } else if (System.currentTimeMillis() > echeance) {
                repondreAuPatient(ACLMessage.FAILURE, "Le docteur n'a pas confirme a temps.");
                etape = 4;
            } else {
                block(200);
            }
        }

        private void repondreAuPatient(int performatif, String contenu) {
            ACLMessage msg = new ACLMessage(performatif);
            msg.addReceiver(patient);
            msg.setContent(contenu);
            msg.setLanguage("Fr");
            msg.setOntology(ONTOLOGIE);
            msg.setConversationId(dossier);
            myAgent.send(msg);
            HopitalGUI.log(getLocalName(), "Reponse envoyee a " + patient.getLocalName());
        }
    }
}
