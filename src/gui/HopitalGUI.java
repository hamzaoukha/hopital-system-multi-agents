package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class HopitalGUI {

    public interface CreateurPatient {
        void creerPatient(String nom, String symptome);
    }

    private static HopitalGUI instance;

    private JFrame fenetre;
    private DefaultTableModel modeleDocteurs;
    private JTextArea zoneLog;
    private JTextField champNom;
    private JComboBox<String> listeSymptomes;
    private JLabel labelCompteur;
    private CreateurPatient createur;
    private int compteurConsultations = 0;

    private static final String[] SYMPTOMES = {
            "douleur thoracique", "fracture du bras", "forte fievre",
            "migraine persistante", "eruption cutanee", "essoufflement",
            "vertige", "toux seche"
    };

    private HopitalGUI() {
        construire();
    }

    public static synchronized HopitalGUI getInstance() {
        if (instance == null) {
            instance = new HopitalGUI();
        }
        return instance;
    }

    public static void demarrer(CreateurPatient createur) {
        HopitalGUI gui = getInstance();
        gui.createur = createur;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getInstance().fenetre.setVisible(true);
            }
        });
    }

    private void construire() {
        fenetre = new JFrame("SMA Hopital - Coordination Infirmier / Docteurs (JADE)");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setSize(900, 620);
        fenetre.setLayout(new BorderLayout(8, 8));

        JPanel haut = new JPanel(new GridLayout(1, 5, 6, 6));
        haut.setBorder(BorderFactory.createTitledBorder("Nouveau patient"));
        haut.add(new JLabel("  Nom :"));
        champNom = new JTextField("Patient" + (int) (Math.random() * 900 + 100));
        haut.add(champNom);
        haut.add(new JLabel("  Symptome :"));
        listeSymptomes = new JComboBox<String>(SYMPTOMES);
        haut.add(listeSymptomes);
        JButton bouton = new JButton("Envoyer aux urgences");
        bouton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (createur == null) {
                    log("GUI", "Aucun conteneur relie a l'interface.");
                    return;
                }
                String nom = champNom.getText().trim();
                if (nom.isEmpty()) {
                    nom = "Patient" + (int) (Math.random() * 900 + 100);
                }
                createur.creerPatient(nom, (String) listeSymptomes.getSelectedItem());
                champNom.setText("Patient" + (int) (Math.random() * 900 + 100));
            }
        });
        haut.add(bouton);
        fenetre.add(haut, BorderLayout.NORTH);

        modeleDocteurs = new DefaultTableModel(
                new Object[]{"Docteur", "Specialite", "File d'attente", "Delai estime (min)", "Etat"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(modeleDocteurs);
        table.setRowHeight(24);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Docteurs enregistres dans le DF"));
        scrollTable.setPreferredSize(new Dimension(880, 200));

        zoneLog = new JTextArea();
        zoneLog.setEditable(false);
        zoneLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        zoneLog.setBackground(new Color(28, 30, 34));
        zoneLog.setForeground(new Color(215, 225, 235));
        JScrollPane scrollLog = new JScrollPane(zoneLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Journal des messages ACL"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, scrollLog);
        split.setDividerLocation(230);
        fenetre.add(split, BorderLayout.CENTER);

        labelCompteur = new JLabel("  Consultations confirmees : 0");
        fenetre.add(labelCompteur, BorderLayout.SOUTH);
    }

    public static void log(final String source, final String message) {
        final String ligne = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] "
                + String.format("%-12s", source) + " | " + message;
        System.out.println(ligne);
        if (instance == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instance.zoneLog.append(ligne + "\n");
                instance.zoneLog.setCaretPosition(instance.zoneLog.getDocument().getLength());
            }
        });
    }

    public static void majDocteur(final String nom, final String specialite,
                                  final int file, final int delai, final String etat) {
        if (instance == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultTableModel m = instance.modeleDocteurs;
                for (int i = 0; i < m.getRowCount(); i++) {
                    if (nom.equals(m.getValueAt(i, 0))) {
                        m.setValueAt(file, i, 2);
                        m.setValueAt(delai, i, 3);
                        m.setValueAt(etat, i, 4);
                        return;
                    }
                }
                m.addRow(new Object[]{nom, specialite, file, delai, etat});
            }
        });
    }

    public static void retirerDocteur(final String nom) {
        if (instance == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultTableModel m = instance.modeleDocteurs;
                for (int i = 0; i < m.getRowCount(); i++) {
                    if (nom.equals(m.getValueAt(i, 0))) {
                        m.removeRow(i);
                        return;
                    }
                }
            }
        });
    }

    public static void consultationConfirmee() {
        if (instance == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                instance.compteurConsultations++;
                instance.labelCompteur.setText("  Consultations confirmees : " + instance.compteurConsultations);
            }
        });
    }
}
