package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestionPointage implements Serializable {
    private static final long serialVersionUID = 1L;

    // La liste observable que la TableView va écouter
    private final transient ObservableList<Check> listePointagesFX = FXCollections.observableArrayList();

    // La liste standard utilisée uniquement pour la sérialisation (car ObservableList n'est pas sérialisable)
    private final List<Check> historiqueGlobal = new ArrayList<>();

    // Ajouter un pointage et mettre à jour la vue en même temps
    public synchronized void ajouterPointage(Check check) {
        historiqueGlobal.add(check);
        // Platform.runLater assure que la modification de l'IHM se fait sur le bon thread JavaFX
        javafx.application.Platform.runLater(() -> listePointagesFX.add(check));
    }

    public ObservableList<Check> getListePointagesFX() {
        return listePointagesFX;
    }

    public List<Check> getHistoriqueGlobal() {
        return historiqueGlobal;
    }

    // Restaurer les données après désérialisation
    public void restaurerHistorique(List<Check> charge) {
        if (charge != null) {
            historiqueGlobal.addAll(charge);
            listePointagesFX.addAll(charge);
        }
    }
}