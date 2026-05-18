package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Employee.Employee;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class PrincipalIHM extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* NAVBAR */

        Button btnEmployee = new Button("Employee");
        Button btnPointage = new Button("Check");
        Button btnParameter = new Button("Parameter");

        btnEmployee.getStyleClass().add("nav-button");
        btnPointage.getStyleClass().add("nav-button");
        btnParameter.getStyleClass().add("nav-button");

        HBox navbar = new HBox(15, btnEmployee, btnPointage, btnParameter);
        navbar.getStyleClass().add("navbar");

        /* TABLE EMPLOYEES */

        TableView<Employee> tableEmployee = new TableView<>();

        TableColumn<Employee, String> colEmpId = new TableColumn<>("UUID");
        TableColumn<Employee, String> colFirstName = new TableColumn<>("Prénom");
        TableColumn<Employee, String> colLastName = new TableColumn<>("Nom");
        TableColumn<Employee, String> colDepartment = new TableColumn<>("Department");

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

        Button btnAddEmployee = new Button("Add Employee");
        Button btnEditEmployee = new Button("Modify Employee");
        Button btnDeleteEmployee = new Button("Delete Employee");

        btnAddEmployee.getStyleClass().add("action-button");
        btnEditEmployee.getStyleClass().add("action-button");
        btnDeleteEmployee.getStyleClass().add("action-button");

        HBox employeeActions = new HBox(10, btnAddEmployee, btnEditEmployee, btnDeleteEmployee);

        VBox pageEmployee = new VBox(10,
                new Label("Employees"),
                employeeActions,
                tableEmployee
        );

        /* TABLE POINTAGE */

        TableView<Check> tablePointage = new TableView<>();

        TableColumn<Check, UUID> colEmpUUID = new TableColumn<>("Employees");
        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Time");
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

        VBox pagePointage = new VBox(10, new Label("Check"), tablePointage);

        VBox pageParameter = new VBox(new Label("Parameter"));

        /*  NAV ACTIONS  */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));
        btnParameter.setOnAction(e -> root.setCenter(pageParameter));

        /*  ROOT  */

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        /*  FENETRE FULL SCREEN  */

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

    public static void main(String[] args) {
        launch();
    }
}