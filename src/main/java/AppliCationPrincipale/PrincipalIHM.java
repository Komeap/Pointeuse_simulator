package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Employee.Employee;
import Entreprise.Department;
import Serveur.Server;

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
    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* BACKEND */
        GestionPointage gestionPointage = new GestionPointage();
        List<Department> departmentsInitiaux = new ArrayList<>();
        departmentsInitiaux.add(new Department("Human Resources"));
        departmentsInitiaux.add(new Department("IT Development"));
        departmentsInitiaux.add(new Department("Accounting"));

        GestionEmployee gestionEmployee = new GestionEmployee(departmentsInitiaux);

        Server monServeur = new Server(gestionPointage);
        monServeur.demarrer();

        /* NAVBAR */

        Button btnEmployee = new Button("Employee");
        Button btnPointage = new Button("Check");

        btnEmployee.getStyleClass().add("nav-button");
        btnPointage.getStyleClass().add("nav-button");

        HBox navbar = new HBox(15, btnEmployee, btnPointage);
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
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

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

        // 1. On crée 3 nouvelles colonnes à la place de la colonne UUID
        TableColumn<Check, String> colCheckFirstName = new TableColumn<>("First Name");
        TableColumn<Check, String> colCheckLastName = new TableColumn<>("Last Name");
        TableColumn<Check, String> colCheckDept = new TableColumn<>("Department");

        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Time");
        TableColumn<Check, CheckType> colType = new TableColumn<>("Type");

        // 2. On configure la logique de recherche pour remplir ces 3 colonnes dynamiquement
        colCheckFirstName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID(); // On récupère l'UUID stocké dans le pointage
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
            return new javafx.beans.property.SimpleStringProperty((emp != null && emp.getDepartment() != null) ? emp.getDepartment().toString() : "None");
        });

        // 3. Les colonnes classiques de Check restent inchangées
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("checkType"));

        // 4. On ajoute toutes les colonnes dans la table
        tablePointage.getColumns().addAll(colCheckFirstName, colCheckLastName, colCheckDept, colDate, colTime, colType);

        //connecte TableView sur la liste des pointages
        //serveur reçoit pointage = l'affiche ici

        ObservableList<Check> checkList =
                FXCollections.observableArrayList(
                        new Check(LocalDate.now(), LocalTime.of(8, 0), CheckType.IN, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(12, 0), CheckType.OUT, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(13, 0), CheckType.IN, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(17, 30), CheckType.OUT, UUID.randomUUID())
                );

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

        /* NAV ACTIONS */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));

        /* ROOT */

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        /* FENETRE FULL SCREEN */

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(
                root,
                screenBounds.getWidth(),
                screenBounds.getHeight()
        );

        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setTitle("Application");
        stage.setScene(scene);
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.show();
    }

    private Employee trouverEmployeeParId(ObservableList<Employee> liste, UUID id) {
        if (id == null) return null;
        for (Employee e : liste) {
            if (id.equals(e.getEmployeeId())) { // On vérifie si l'UUID correspond
                return e;
            }
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