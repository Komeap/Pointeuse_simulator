package AppliCationPrincipale;

import Employee.Employee;
import Entreprise.Department;
import Serveur.Serialisation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionEmployee {
    private ObservableList<Employee> employeeList;
    private ObservableList<Department> departmentList;
    private static final String fileName = "employees.ser"; //nom du fichier contenant les employés

    //Ici on init les employees par défaut ais faudra le modif merci
    public GestionEmployee(List<Department> departments) {
        this.employeeList = FXCollections.observableArrayList();
        this.departmentList = (ObservableList<Department>) departments;

        List<Employee> loadFile = (List<Employee>) Serialisation.loadObject(fileName);
        if (loadFile != null && !loadFile.isEmpty()) {
            this.employeeList.addAll(loadFile);
        }
    }

    public ObservableList<Employee> getEmployeeList()
    {
        return employeeList;
    }


    public void ajouterEmployee() {
        // Création d'une boîte de dialogue personnalisée
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un employé");
        dialog.setHeaderText("Veuillez remplir les informations de l'employé");

        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        // Création des champs de saisie
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        ComboBox<Department> deptComboBox = new ComboBox<>();
        deptComboBox.setItems(departmentList);
        deptComboBox.setPromptText("Sélectionner un département");

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Département:"), 0, 2);
        grid.add(deptComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        //conversion du résultat quand on clique sur "Ajouter"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {

                //création d'un planning par défaut pour nouvel employé
                Planning.Planning defaultPlanning = new Planning.Planning();

                // On définit une journée type
                Planning.WorkDay journeeType = new Planning.WorkDay(
                        java.time.LocalTime.of(8, 0),
                        java.time.LocalTime.of(17, 0)
                );

                //on ajoute pour les jours de la semaine
                defaultPlanning.setWorkDay(java.time.DayOfWeek.MONDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.TUESDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.WEDNESDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.THURSDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.FRIDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.SATURDAY, journeeType);
                defaultPlanning.setWorkDay(java.time.DayOfWeek.SUNDAY, journeeType);

                return new Employee(firstNameField.getText(), lastNameField.getText(), deptComboBox.getValue(), defaultPlanning);
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            employeeList.add(employee);
            if(employee.getDepartment() != null) {
                employee.getDepartment().addEmployee(employee);
            }
            sauvegarderDonnees();
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

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Modifier un employé");
        dialog.setHeaderText("Modification de : " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());

        ButtonType btnValider = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // On pré-remplit les champs avec les données actuelles
        TextField firstNameField = new TextField(selectedEmployee.getFirstName());
        TextField lastNameField = new TextField(selectedEmployee.getLastName());
        ComboBox<Department> deptComboBox = new ComboBox<>();
        deptComboBox.setItems(departmentList);
        deptComboBox.setValue(selectedEmployee.getDepartment());

        grid.add(new Label("Prénom:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Département:"), 0, 2);
        grid.add(deptComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                selectedEmployee.setFirstName(firstNameField.getText());
                selectedEmployee.setLastName(lastNameField.getText());

                // Gestion propre du changement de département
                Department ancienDept = selectedEmployee.getDepartment();
                Department nouveauDept = deptComboBox.getValue();

                if (ancienDept != null && !ancienDept.equals(nouveauDept)) {
                    ancienDept.removeEmployee(selectedEmployee); // On le retire de l'ancien
                }
                if (nouveauDept != null && !nouveauDept.equals(ancienDept)) {
                    nouveauDept.addEmployee(selectedEmployee); // On l'ajoute au nouveau
                }

                selectedEmployee.setDepartment(nouveauDept);
                return selectedEmployee;
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            // Force le rafraîchissement visuel de la table
            int index = employeeList.indexOf(employee);
            employeeList.set(index, employee);
            sauvegarderDonnees();
        });
    }

    public void supprimerEmployee(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            afficherAlerteSelection();
            return;
        }
        if (selectedEmployee.getDepartment() != null) {
            selectedEmployee.getDepartment().removeEmployee(selectedEmployee);
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
