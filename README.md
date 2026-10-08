# hopital-sma1
# Hôpital SMA

Système multi-agents pour la gestion des urgences hospitalières, développé avec la plateforme **JADE**.

Un patient arrive aux urgences avec un symptôme. Un infirmier fait le triage, puis négocie avec plusieurs docteurs grâce au protocole **Contract Net** pour choisir le docteur le plus adapté. Le choix ne repose pas seulement sur la rapidité : il combine le délai, la charge de travail et la spécialité, et le poids du délai augmente avec l'urgence du patient.

Projet réalisé dans le cadre du module *Intelligence Artificielle et Systèmes Multi-Agents*, Master IAA, Faculté Polydisciplinaire de Ouarzazate (Université Ibn Zohr), sous l'encadrement de Dr. Imane Dbibih.

## Les agents

| Agent | Rôle |
|---|---|
| `PatientAgent` | Envoie une demande (REQUEST) avec son symptôme |
| `InfirmierAgent` | Fait le triage, cherche les docteurs dans le DF, lance la négociation et choisit le meilleur |
| `DocteurAgent` | Publie sa spécialité dans le DF, répond aux appels par PROPOSE ou REFUSE |

## Déroulement d'une négociation

```
Patient   --REQUEST-->  Infirmier          (symptôme)
Infirmier --search-->   DF                 (service "Consultation")
Infirmier --CFP-->      Docteurs           (spécialité ; urgence)
Docteur   --PROPOSE-->  Infirmier          (délai ; file ; spécialité)  ou REFUSE si saturé
Infirmier --ACCEPT_PROPOSAL--> gagnant,  REJECT_PROPOSAL --> les autres
Docteur   --INFORM-->   Infirmier --INFORM--> Patient
```

Chaque négociation a son propre `conversationId` (`DOS-001`, `DOS-002`, ...), ce qui permet de traiter plusieurs patients en parallèle.

## Fonction de score

```
score = (1 + urgence) × délai  +  5 × file_d_attente  +  (50 si mauvaise spécialité)
```

La proposition avec le **score le plus bas** gagne. Exemple réel : pour une douleur thoracique (urgence 4), Dr Chafik, généraliste, propose 5 min (score 75) et perd contre Dr Alami, cardiologue, qui propose 10 min (score 50).

## Installation et lancement

Le projet ne dépend que de **JADE 4.6.0** (`jade.jar`), qui n'est pas inclus dans ce dépôt.

1. Téléchargez JADE sur [jade.tilab.com](https://jade.tilab.com/) et copiez `jade.jar` dans le dossier `lib/`.
2. **Avec Eclipse** : `File` → `Import` → `Existing Projects into Workspace`, choisissez ce dossier, puis lancez `src/containers/LancerTout.java` (`Run As` → `Java Application`). Le répertoire de travail doit être la racine du projet, pour que `data/docteurs.json` soit trouvé.
3. **Sans Eclipse (Windows)** : lancez `compiler.bat`, puis `lancer.bat`.

Deux fenêtres s'ouvrent : la console JADE (RMA) et l'interface de l'hôpital. Le bouton « Envoyer aux urgences » crée un nouveau patient en direct.

Pour lancer chaque conteneur dans un processus séparé, exécutez dans l'ordre : `MainContainerLauncher`, `DocteurContainer`, `InfirmierContainer`, `PatientContainer`.

## Structure

```
src/
  agents/      PatientAgent, InfirmierAgent, DocteurAgent
  containers/  LancerTout et les conteneurs séparés
  model/       Triage, Evaluateur (score), Docteur, Consultation
  util/        SimpleJson, Persistance
  gui/         HopitalGUI (Swing)
data/
  docteurs.json        liste des docteurs, lue au démarrage
  consultations.json   historique, écrit après chaque consultation
docs/
  rapport_HopitalSMA.pdf
  presentation_HopitalSMA.pptx
```

Pour ajouter un docteur, il suffit de modifier `data/docteurs.json`, sans toucher au code.

## Auteurs

**Réalisé par :** Hamza Oukhacha, Othman El Blaidi, Mohamed Archaki.

**Encadré par :** Dr. Imane Dbibih.