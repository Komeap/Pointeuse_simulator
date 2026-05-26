package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Serveur.Serialisation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestionPointage implements Serializable {

    private static final String fileName = "pointages.ser";

    // La liste observable que la TableView va écouter
    private final transient ObservableList<Check> listePointagesFX = FXCollections.observableArrayList();

    // La liste standard utilisée uniquement pour la sérialisation (car ObservableList n'est pas sérialisable)
    private final List<Check> historiqueGlobal = new ArrayList<>();

    public GestionPointage() {
        @SuppressWarnings("unchecked")
        List<Check> historiqueCharge = (List<Check>) Serialisation.loadObject(fileName);

        if (historiqueCharge != null && !historiqueCharge.isEmpty()) {
            this.historiqueGlobal.addAll(historiqueCharge);
            this.listePointagesFX.addAll(historiqueCharge);
            System.out.println("Historique des pointages restauré : " + historiqueGlobal.size() + " éléments.");
        } else {
            System.out.println("Aucun historique de pointage trouvé. Création d'un nouveau fichier.");
        }
    }

    // Ajouter un pointage et mettre à jour la vue en même temps
    public synchronized void ajouterPointage(Check check) {
        historiqueGlobal.add(check);
        sauvegarderDonnees();
        // Platform.runLater assure que la modification de l'IHM se fait sur le bon thread JavaFX
        javafx.application.Platform.runLater(() -> listePointagesFX.add(check));
    }

    public ObservableList<Check> getListePointagesFX() {
        return listePointagesFX;
    }

    public List<Check> getHistoriqueGlobal() {
        return historiqueGlobal;
    }

    private void sauvegarderDonnees()
    {
        Serialisation.saveObject(new ArrayList<>(historiqueGlobal), fileName);
    }

    // Restaurer les données après désérialisation
    public void restaurerHistorique(List<Check> charge) {
        if (charge != null) {
            historiqueGlobal.addAll(charge);
            listePointagesFX.addAll(charge);
        }
    }

    public void supprimerPointage(Check selectedCheck)
    {
        if (selectedCheck == null) {
            afficherAlerteSelection();
            return;
        }
        historiqueGlobal.remove(selectedCheck);
        javafx.application.Platform.runLater(() -> listePointagesFX.remove(selectedCheck));
        sauvegarderDonnees();
    }

    public void modifierPointage(Check selectedCheck) {
        if (selectedCheck == null) {
            afficherAlerteSelection();
            return;
        }

        javafx.scene.control.ChoiceDialog<CheckType> dialog = new javafx.scene.control.ChoiceDialog<>(
                selectedCheck.getCheckType(),
                java.util.Arrays.asList(CheckType.values())
        );

        dialog.setTitle("Modifier un pointage");
        dialog.setContentText("Choisir le nouveau type :");

        java.util.Optional<CheckType> result = dialog.showAndWait();

        result.ifPresent(newType -> {
            // FIX : On récupère les index D'ABORD, tant que l'objet correspond parfaitement à ce qu'il y a dans les listes
            int indexGlobal = historiqueGlobal.indexOf(selectedCheck);
            int indexFX = listePointagesFX.indexOf(selectedCheck);

            // Maintenant, on peut modifier l'objet métier en toute sécurité
            selectedCheck.setCheckType(newType);

            // Mise à jour de la liste de sauvegarde
            if (indexGlobal != -1) {
                historiqueGlobal.set(indexGlobal, selectedCheck);
            }

            // Mise à jour visuelle garantie sur le thread JavaFX
            if (indexFX != -1) {
                javafx.application.Platform.runLater(() -> {
                    listePointagesFX.set(indexFX, selectedCheck);
                });
            }

            sauvegarderDonnees();
        });
    }

    private void afficherAlerteSelection()
    {
        // Platform.runLater garantit que la pop-up s'ouvre sur le thread principal de l'IHM
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Sélection requise");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un pointage dans le tableau avant d'effectuer cette action.");
            alert.showAndWait();
        });
    }
}