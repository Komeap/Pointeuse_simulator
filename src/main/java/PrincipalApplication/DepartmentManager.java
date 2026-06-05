/**
 * The 'DepartmentManager' class allows you to manipulate a list to display your department in your principal interface.
 * It also contains all the function to add/remove/modify a department.
 */
package PrincipalApplication;

import Employee.Employee;
import Entreprise.Department;
import Serialization.Serialization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class DepartmentManager {

    //list of departments displayed in JavaFX
    private ObservableList<Department> departmentList;

    //file name used for serialization
    private static final String fileName = "departments.ser";

    //constructor initializes the department list and loads saved data if available
    public DepartmentManager(List<Department> initialDepartments) {

        this.departmentList = FXCollections.observableArrayList();

        //we try to load previously saved departments from file
        List<Department> loaded =
                (List<Department>) Serialization.loadObject(fileName);

        //if data exists, we restore it, otherwise we use initial data
        if (loaded != null && !loaded.isEmpty()) {
            this.departmentList.addAll(loaded);
        } else {
            this.departmentList.addAll(initialDepartments);
        }
    }

    //returns observable list for UI binding
    public ObservableList<Department> getDepartmentList() {
        return departmentList;
    }

    //opens a dialog to add a new department
    public void addDepartment() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Department");
        dialog.setHeaderText(null);
        dialog.setContentText("Department name :");

        dialog.showAndWait().ifPresent(name -> {

            //we check that the input is not empty
            if (!name.trim().isEmpty()) {

                //we create and add the new department
                departmentList.add(new Department(name));

                //we save the updated list
                save();
            }
        });
    }

    //removes a department and removes its reference from employees
    public void supprimerDepartment(Department dep,
                                    ObservableList<Employee> employees) {

        //safety check
        if (dep == null) return;

        //we remove department reference from all employees linked to it
        for (Employee emp : employees) {

            if (emp.getDepartment() != null &&
                    emp.getDepartment().equals(dep)) {

                emp.setDepartment(null);
            }
        }

        //we remove the department from the list
        departmentList.remove(dep);

        //we save changes
        save();
    }

    //saves department list to file
    private void save() {
        Serialization.saveObject(
                new ArrayList<>(departmentList),
                fileName
        );
    }
}