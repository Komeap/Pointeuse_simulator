package AppliCationPrincipale;

import Check.Check;
import Check.CheckType;
import Employee.Employee;
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
import java.util.UUID;

public class PrincipalIHM extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* BACKEND */
        GestionPointage gestionPointage = new GestionPointage();
        GestionEmployee gestionEmployee = new GestionEmployee();

        Server monServeur = new Server(gestionPointage);
        monServeur.demarrer();

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

        VBox pageEmployee = new VBox(10,
                new Label("Employees"),
                employeeActions,
                tableEmployee
        );

        pageEmployee.setPadding(new Insets(15));

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

        //connecte TableView sur la liste des pointages
        //serveur reçoit pointage = l'affiche ici
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

        /* PAGE PARAMETRES */

        Label lblTitleParam = new Label("Application Parameters");


        // Port
        Label lblPort = new Label("Port :");
        TextField txtPort = new TextField();
        txtPort.setPromptText("5001");

        // Nom application
        Label lblAppName = new Label("Application Name :");
        TextField txtAppName = new TextField();
        txtAppName.setPromptText("PointageApp");

        // Temps de rafraîchissement
        Label lblRefresh = new Label("Refresh Delay (sec) :");
        Spinner<Integer> refreshSpinner = new Spinner<>(1, 60, 5);

        // Activation notifications
        CheckBox cbNotifications = new CheckBox("Enable notifications");

        // Bouton sauvegarde
        Button btnSaveParams = new Button("Save Parameters");
        btnSaveParams.getStyleClass().add("action-button");

        btnSaveParams.setOnAction(e -> {

            String appName = txtAppName.getText();
            int refresh = refreshSpinner.getValue();
            boolean notifications = cbNotifications.isSelected();

            try {

                int portValue = Integer.parseInt(txtPort.getText());

                Server.changerPort(portValue);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Parameters Saved");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Port serveur : " + portValue +
                                "\nApplication : " + appName +
                                "\nRefresh : " + refresh + " sec" +
                                "\nNotifications : " + notifications
                );

                alert.showAndWait();

            } catch (Exception ex) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Port invalide");

                alert.showAndWait();
            }
        });


        GridPane paramGrid = new GridPane();
        paramGrid.setHgap(10);
        paramGrid.setVgap(15);

        paramGrid.add(lblPort, 0, 1);
        paramGrid.add(txtPort, 1, 1);

        paramGrid.add(lblAppName, 0, 2);
        paramGrid.add(txtAppName, 1, 2);

        paramGrid.add(lblRefresh, 0, 3);
        paramGrid.add(refreshSpinner, 1, 3);

        paramGrid.add(cbNotifications, 1, 4);

        VBox pageParameter = new VBox(20,
                lblTitleParam,
                paramGrid,
                btnSaveParams
        );

        pageParameter.setPadding(new Insets(20));

        /* NAV ACTIONS */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));
        btnParameter.setOnAction(e -> root.setCenter(pageParameter));

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

    public static void main(String[] args) {
        launch();
    }
}