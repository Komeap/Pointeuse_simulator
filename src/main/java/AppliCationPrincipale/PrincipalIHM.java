package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Employee.Employee;
import Entreprise.Department;
import Serveur.Server;
import Serveur.Serialisation;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrincipalIHM extends Application {

    private static final String DEPARTMENT_FILE = "departments.ser";

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* BACKEND */
        GestionPointage gestionPointage = new GestionPointage();

        List<Department> departmentsInitiaux = new ArrayList<>();
        departmentsInitiaux.add(new Department("Human Resources"));
        departmentsInitiaux.add(new Department("IT Development"));
        departmentsInitiaux.add(new Department("Accounting"));

        ObservableList<Department> departments = FXCollections.observableArrayList();

        @SuppressWarnings("unchecked")
        List<Department> loaded = (List<Department>) Serialisation.loadObject(DEPARTMENT_FILE);

        if (loaded != null) {
            departments.addAll(loaded);
        } else {
            // Si le fichier n'existe pas encore, on charge ceux par défaut
            departments.addAll(departmentsInitiaux);
        }

        GestionEmployee gestionEmployee = new GestionEmployee(departments);

        Server monServeur = new Server(gestionPointage);
        monServeur.demarrer();

        /* NAVBAR */

        Button btnEmployee = new Button("Employee");
        Button btnPointage = new Button("Check");
        Button btnDepartment = new Button("Department");

        btnEmployee.getStyleClass().add("nav-button");
        btnPointage.getStyleClass().add("nav-button");
        btnDepartment.getStyleClass().add("nav-button");

        HBox navbar = new HBox(15, btnEmployee, btnPointage, btnDepartment);
        navbar.getStyleClass().add("navbar");

        /* TABLE EMPLOYEES */

        TableView<Employee> tableEmployee = new TableView<>();

        TableColumn<Employee, String> colEmpId = new TableColumn<>("UUID");
        TableColumn<Employee, String> colFirstName = new TableColumn<>("First Name");
        TableColumn<Employee, String> colLastName = new TableColumn<>("Last Name");
        TableColumn<Employee, String> colDepartment = new TableColumn<>("Department");

        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDepartment.setCellValueFactory(cellData -> {
            Department dep = cellData.getValue().getDepartment();
            return new javafx.beans.property.SimpleStringProperty(
                    dep == null ? "N/A" : dep.getDepartement()
            );
        });

        tableEmployee.getColumns().addAll(colEmpId, colFirstName, colLastName, colDepartment);

        tableEmployee.setItems(gestionEmployee.getEmployeeList());
        tableEmployee.setPrefHeight(500);
        tableEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAddEmployee = new Button("Add Employee");
        Button btnEditEmployee = new Button("Modify Employee");
        Button btnDeleteEmployee = new Button("Delete Employee");

        HBox employeeActions = new HBox(10, btnAddEmployee, btnEditEmployee, btnDeleteEmployee);

        btnAddEmployee.setOnAction(e -> gestionEmployee.ajouterEmployee());

        btnEditEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            gestionEmployee.modifierEmployee(selection);
        });

        btnDeleteEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            gestionEmployee.supprimerEmployee(selection);
        });

        PlanningService planningService = new PlanningService();
        HBox barrePlanningVisualisation = new HBox();
        Label lblInfoPlanning = new Label("Select an employee to display their schedule");
        lblInfoPlanning.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        HBox selecteurJours = new HBox(10);
        ToggleGroup groupeJours = new ToggleGroup();

        java.time.DayOfWeek[] joursSemaine = {
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY,
                java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        };

        String[] nomsJours = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 0; i < joursSemaine.length; i++) {
            ToggleButton btnJour = new ToggleButton(nomsJours[i]);
            btnJour.setToggleGroup(groupeJours);
            btnJour.setUserData(joursSemaine[i]);
            if (i == 0) btnJour.setSelected(true);
            selecteurJours.getChildren().add(btnJour);
        }

        tableEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mettreAJourPlanningVisuel(tableEmployee, groupeJours, planningService, barrePlanningVisualisation, lblInfoPlanning);
        });

        groupeJours.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                groupeJours.selectToggle(oldVal);
            } else {
                mettreAJourPlanningVisuel(tableEmployee, groupeJours, planningService, barrePlanningVisualisation, lblInfoPlanning);
            }
        });

        VBox pageEmployee = new VBox(15,
                new Label("Employees"),
                employeeActions,
                tableEmployee,
                new Separator(),
                selecteurJours,
                lblInfoPlanning,
                barrePlanningVisualisation
        );

        pageEmployee.setPadding(new Insets(15));

        /* TABLE POINTAGE */

        TableView<Check> tablePointage = new TableView<>();

        // Les colonnes en anglais uniquement
        TableColumn<Check, String> colCheckFirstName = new TableColumn<>("First Name");
        TableColumn<Check, String> colCheckLastName = new TableColumn<>("Last Name");
        TableColumn<Check, String> colCheckDept = new TableColumn<>("Department");

        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Time");
        TableColumn<Check, CheckType> colType = new TableColumn<>("Type");

        colCheckFirstName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = trouverEmployeeParId(gestionEmployee.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getFirstName() : "Unknown");
        });

        colCheckLastName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = trouverEmployeeParId(gestionEmployee.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getLastName() : "Unknown");
        });

        colCheckDept.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = trouverEmployeeParId(gestionEmployee.getEmployeeList(), empId);

            return new javafx.beans.property.SimpleStringProperty(
                    (emp != null && emp.getDepartment() != null)
                            ? emp.getDepartment().getDepartement()
                            : "N/A"
            );
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("checkType"));

        tablePointage.getColumns().addAll(colCheckFirstName, colCheckLastName, colCheckDept, colDate, colTime, colType);

        tablePointage.setItems(gestionPointage.getListePointagesFX());
        tablePointage.setPrefHeight(500);
        tablePointage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnEditCheck = new Button("Modify Check");
        Button btnDeleteCheck = new Button("Delete Check");

        HBox checkActions = new HBox(10, btnEditCheck, btnDeleteCheck);

        btnEditCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            gestionPointage.modifierPointage(selection);
        });

        btnDeleteCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            gestionPointage.supprimerPointage(selection);
        });

        VBox pagePointage = new VBox(10,
                new Label("Check"),
                checkActions,
                tablePointage
        );

        pagePointage.setPadding(new Insets(15));

        /* TABLE DEPARTMENT */

        TableView<Department> tableDepartment = new TableView<>();

        TableColumn<Department, String> colDepName = new TableColumn<>("Department");
        colDepName.setCellValueFactory(new PropertyValueFactory<>("departement"));

        tableDepartment.getColumns().add(colDepName);
        tableDepartment.setItems(departments);

        tableDepartment.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAddDepartment = new Button("Add Department");
        Button btnDeleteDepartment = new Button("Delete Department");

        btnAddDepartment.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Department");
            dialog.setContentText("Department name:");

            dialog.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    Department d = new Department(name);
                    departments.add(d);
                    Serialisation.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
                }
            });
        });

        btnDeleteDepartment.setOnAction(e -> {
            Department dep = tableDepartment.getSelectionModel().getSelectedItem();
            if (dep == null) return;

            // unlink employees
            for (Employee emp : gestionEmployee.getEmployeeList()) {
                if (dep.equals(emp.getDepartment())) {
                    emp.setDepartment(null);
                }
            }

            // remove department
            departments.remove(dep);

            // refresh tables that depend on it
            tableEmployee.refresh();
            tablePointage.refresh();

            Serialisation.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
        });

        HBox departmentActions = new HBox(10, btnAddDepartment, btnDeleteDepartment);

        VBox pageDepartment = new VBox(10,
                new Label("Departments"),
                departmentActions,
                tableDepartment
        );

        pageDepartment.setPadding(new Insets(15));

        /* NAV */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));
        btnDepartment.setOnAction(e -> root.setCenter(pageDepartment));

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Application");
        stage.show();
    }

    private Employee trouverEmployeeParId(ObservableList<Employee> liste, UUID id) {
        if (id == null) return null;
        for (Employee e : liste) {
            if (id.equals(e.getEmployeeId())) return e;
        }
        return null;
    }

    public static void main(String[] args) {
        launch();
    }

    private void mettreAJourPlanningVisuel(TableView<Employee> table, ToggleGroup groupe, PlanningService service, HBox barre, Label label) {
        Employee empSelectionne = table.getSelectionModel().getSelectedItem();
        ToggleButton jourSelectionne = (ToggleButton) groupe.getSelectedToggle();

        if (empSelectionne != null && jourSelectionne != null) {
            java.time.DayOfWeek jour = (java.time.DayOfWeek) jourSelectionne.getUserData();
            service.chargerPlanning(barre, label, empSelectionne, jour);
        }
    }
}