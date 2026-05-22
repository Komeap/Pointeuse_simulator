package AppliCationPrincipale;

import Employee.Employee;
import Serveur.Serialisation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionEmployee {
    private ObservableList<Employee> employeeList;
    private static final String fileName = "employees.ser"; //nom du fichier contenant les employés

    //Ici on init les employees par défaut ais faudra le modif merci
    public GestionEmployee() {
        this.employeeList = FXCollections.observableArrayList();

        List<Employee> loadFile = (List<Employee>) Serialisation.loadObject(fileName);
        if (loadFile != null && !loadFile.isEmpty()) {
            this.employeeList.addAll(loadFile);
        }else{
            this.employeeList.addAll(
                new Employee("Jean", "Dupont", null, null),
                new Employee("Marie", "Leroy", null, null),
                new Employee("Lucas", "Martin", null, null)
            );
            sauvegarderDonnees();
        }
    }

    public ObservableList<Employee> getEmployeeList()
    {
        return employeeList;
    }

    public void ajouterEmployee()
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter un employe");
        dialog.setHeaderText("Création d'un nouvel employe");
        dialog.setContentText("Entrez le nom prenom :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                employeeList.add(new Employee(parts[0], parts[1], null, null));
                sauvegarderDonnees();
            }
        });
    }

    private void sauvegarderDonnees() {
        // On convertit l'ObservableList en ArrayList classique pour la sérialisation
        Serialisation.saveObject(new ArrayList<>(employeeList), fileName);
    }

    public void modifierEmployee(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            afficherAlerteSelection();
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
        dialog.setTitle("Modifier un employé");
        //dialog.setHeaderText("Modification de : " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
        dialog.setContentText("Modifier le prénom et le nom :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                selectedEmployee.setFirstName(parts[0]);
                selectedEmployee.setLastName(parts[1]);
                //Astuce JavaFX : force le rafraîchissement visuel de la table
                int index = employeeList.indexOf(selectedEmployee);
                employeeList.set(index, selectedEmployee);
                sauvegarderDonnees();
            }
        });
    }

    public void supprimerEmployee(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            afficherAlerteSelection();
            return;
        }
        employeeList.remove(selectedEmployee);
        sauvegarderDonnees();
    }

    private void afficherAlerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sélection manquante");
        alert.setHeaderText(null);
        alert.setContentText("Veuillez sélectionner un employé dans le tableau d'abord.");
        alert.showAndWait();
    }
}
