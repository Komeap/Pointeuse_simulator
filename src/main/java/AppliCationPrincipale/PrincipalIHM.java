package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Employee.Employee;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class PrincipalIHM extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* ================= NAVBAR ================= */

        Button btnEmployee = new Button("Employés");
        Button btnPointage = new Button("Pointages");
        Button btnParameter = new Button("Paramètres");

        HBox navbar = new HBox(btnEmployee, btnPointage, btnParameter);
        navbar.getStyleClass().add("navbar");

        /* ================= TABLE EMPLOYEES ================= */

        TableView<Employee> tableEmployee = new TableView<>();

        TableColumn<Employee, String> colEmpId = new TableColumn<>("UUID");
        TableColumn<Employee, String> colFirstName = new TableColumn<>("Prénom");
        TableColumn<Employee, String> colLastName = new TableColumn<>("Nom");
        TableColumn<Employee, String> colDepartment = new TableColumn<>("Département");

        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

        tableEmployee.getColumns().addAll(colEmpId, colFirstName, colLastName, colDepartment);

        ObservableList<Employee> employeeList =
                FXCollections.observableArrayList(
                        new Employee("Jean", "Dupont", null, null),
                        new Employee("Marie", "Leroy", null, null),
                        new Employee("Lucas", "Martin", null, null)
                );

        tableEmployee.setItems(employeeList);
        tableEmployee.setPrefHeight(500);
        tableEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox pageEmployee = new VBox(10, new Label("Employés"), tableEmployee);

        /* ================= TABLE POINTAGE ================= */

        TableView<Check> tablePointage = new TableView<>();

        TableColumn<Check, UUID> colEmpUUID = new TableColumn<>("Employé");
        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Heure");
        TableColumn<Check, CheckType> colType = new TableColumn<>("Type");

        colEmpUUID.setCellValueFactory(new PropertyValueFactory<>("employeeUUID"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("checkType"));

        tablePointage.getColumns().addAll(colEmpUUID, colDate, colTime, colType);

        ObservableList<Check> checkList =
                FXCollections.observableArrayList(
                        new Check(LocalDate.now(), LocalTime.of(8, 0), CheckType.IN, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(12, 0), CheckType.OUT, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(13, 0), CheckType.IN, UUID.randomUUID()),
                        new Check(LocalDate.now(), LocalTime.of(17, 30), CheckType.OUT, UUID.randomUUID())
                );

        tablePointage.setItems(checkList);
        tablePointage.setPrefHeight(500);
        tablePointage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox pagePointage = new VBox(10, new Label("Pointages"), tablePointage);

        VBox pageParameter = new VBox(new Label("Paramètres"));

        /* ================= NAV ACTIONS ================= */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));
        btnParameter.setOnAction(e -> root.setCenter(pageParameter));

        /* ================= ROOT ================= */

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}