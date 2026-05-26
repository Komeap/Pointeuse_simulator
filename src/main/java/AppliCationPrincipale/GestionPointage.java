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
import java.util.UUID;

public class GestionPointage implements Serializable {
    private static final long serialVersionUID = 1L;

    // La liste observable que la TableView va écouter
    private final transient ObservableList<Check> listePointagesFX = FXCollections.observableArrayList();

    // La liste standard utilisée uniquement pour la sérialisation (car ObservableList n'est pas sérialisable)
    private final List<Check> historiqueGlobal = new ArrayList<>();

    // --------------------------------------------------------
    // --------------------------------------------------------

    /**
     * Construit un Check avec le bon CheckType calculé automatiquement.
     * À appeler depuis le Serveur au lieu de créer le Check manuellement.
     */
    public Check creerPointageAutomatique(UUID employeeId, LocalDate date, LocalTime time) {
        CheckType typeCalcule = determinerProchainType(employeeId, date);
        return new Check(date, time, typeCalcule, employeeId);
    }

    /**
     * Détermine  IN ou OUT.
     */
    private CheckType determinerProchainType(UUID employeeId, LocalDate dateDuNouveauPointage) {
        Check dernierPointage = getLastCheckForEmployee(employeeId);

        //Si aucun pointage précédent, c'est IN
        if (dernierPointage == null) {
            return CheckType.IN;
        }

        LocalDate dateDernierPointage = dernierPointage.getDate();
        CheckType typeDernierPointage = dernierPointage.getCheckType();

        // On est sur un jour différent (le lendemain ou plus tard)
        if (dateDuNouveauPointage.isAfter(dateDernierPointage)) {
            // Si le dernier pointage de la veille était un IN, il y a eu un oubli
            if (typeDernierPointage == CheckType.IN) {
                System.out.println("ALERTE : Oubli de pointage OUT détecté pour l'employé " + employeeId + " le " + dateDernierPointage);
            }
            // Dans tous les cas le premier pointage d'une nouvelle journée est un IN
            return CheckType.IN;
        }

        // On est le même jour, on change le type
        if (dateDuNouveauPointage.isEqual(dateDernierPointage)) {
            return (typeDernierPointage == CheckType.IN) ? CheckType.OUT : CheckType.IN;
        }

        // Sécurité par défaut, pour gerer les eventuel probleme de date
        return CheckType.IN;
    }

    /**
     * Récupère le tout dernier pointage enregistré pour un employé spécifique.
     */
    private Check getLastCheckForEmployee(UUID employeeId) {
        // On parcourt la liste à l'envers pour trouver le plus récent
        for (int i = historiqueGlobal.size() - 1; i >= 0; i--) {
            Check c = historiqueGlobal.get(i);
            if (c.getEmployeeUUID().equals(employeeId)) {
                return c;
            }
        }
        return null;
    }

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