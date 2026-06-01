package AppliCationPrincipale;

import Employee.Employee;
import Entreprise.Department;
import Serveur.Serialisation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionDepartment {

    private ObservableList<Department> departmentList;
    private static final String fileName = "departments.ser";

    public GestionDepartment(List<Department> initialDepartments) {

        this.departmentList = FXCollections.observableArrayList();

        List<Department> loaded =
                (List<Department>) Serialisation.loadObject(fileName);

        if (loaded != null && !loaded.isEmpty()) {
            this.departmentList.addAll(loaded);
        } else {
            this.departmentList.addAll(initialDepartments);
        }
    }

    public ObservableList<Department> getDepartmentList() {
        return departmentList;
    }

    public void ajouterDepartment() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter département");
        dialog.setHeaderText(null);
        dialog.setContentText("Nom département:");

        dialog.showAndWait().ifPresent(name -> {

            if (!name.trim().isEmpty()) {

                departmentList.add(new Department(name));
                sauvegarder();
            }
        });
    }

    public void supprimerDepartment(Department dep,
                                    ObservableList<Employee> employees) {

        if (dep == null) return;

        for (Employee emp : employees) {

            if (emp.getDepartment() != null &&
                    emp.getDepartment().equals(dep)) {

                emp.setDepartment(null);
            }
        }

        departmentList.remove(dep);
        sauvegarder();
    }

    // 💾 SAVE
    private void sauvegarder() {
        Serialisation.saveObject(
                new ArrayList<>(departmentList),
                fileName
        );
    }
}