package AppliCationPrincipale;

import Employee.Employee;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;

public class PrincipalIHM extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /*
         * NAVBAR
         */

        Button btnEmployee = new Button("Gestion des employées");
        Button btnPointage = new Button("Gestion des pointages");
        Button btnParameter = new Button("Paramètres");

        btnEmployee.getStyleClass().add("nav-button");
        btnPointage.getStyleClass().add("nav-button");
        btnParameter.getStyleClass().add("nav-button");

        HBox navbar = new HBox(
                btnEmployee,
                btnPointage,
                btnParameter
        );

        navbar.getStyleClass().add("navbar");
        /*
         * TABLE POINTAGES
         */
        TableView<Check> tablePointage = new TableView<>();

        // Colonnes
        TableColumn<Employee, String> colId =
                new TableColumn<>("UUID");

        TableColumn<Employee, String> colFirstName =
                new TableColumn<>("Date");

        TableColumn<Employee, String> colLastName =
                new TableColumn<>("Time");

        TableColumn<Employee, String> colDepartment =
                new TableColumn<>("Département");

        /*
         * TABLE EMPLOYEES
         */

        TableView<Employee> tableEmployee = new TableView<>();

        // Colonnes
        TableColumn<Employee, String> colId =
                new TableColumn<>("UUID");

        TableColumn<Employee, String> colFirstName =
                new TableColumn<>("Prénom");

        TableColumn<Employee, String> colLastName =
                new TableColumn<>("Nom");

        TableColumn<Employee, String> colDepartment =
                new TableColumn<>("Département");

        // Liaison getters
        colId.setCellValueFactory(
                new PropertyValueFactory<>("employeeId")
        );

        colFirstName.setCellValueFactory(
                new PropertyValueFactory<>("firstName")
        );

        colLastName.setCellValueFactory(
                new PropertyValueFactory<>("lastName")
        );

        colDepartment.setCellValueFactory(
                new PropertyValueFactory<>("department")
        );


        // Ajouter colonnes
        tableEmployee.getColumns().addAll(
                colId,
                colFirstName,
                colLastName,
                colDepartment
        );

        // Resize auto
        tableEmployee.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        /*
         * DONNÉES
         */

        ObservableList<Employee> employees =
                FXCollections.observableArrayList(new Employee("Jean", "Dupont", null, null));
        tableEmployee.setItems(employees);
        tableEmployee.setPrefHeight(500);
        tableEmployee.setPrefWidth(800);
        /*
         * PAGE EMPLOYEE
         */

        VBox pageEmployee = new VBox(10);

        pageEmployee.getChildren().addAll(
                new Label("Gestion des employés"),
                tableEmployee
        );


        /*
         * PAGE POINTAGE
         */

        VBox pagePointage = new VBox(
                new Label("Pointage")
        );

        /*
         * PAGE PARAMETER
         */

        VBox pageParameter = new VBox(
                new Label("Paramètres")
        );

        pageEmployee.getStyleClass().add("page1");
        pagePointage.getStyleClass().add("page2");
        pageParameter.getStyleClass().add("page3");

        /*
         * BOUTONS
         */

        btnEmployee.setOnAction(
                e -> root.setCenter(pageEmployee)
        );

        btnPointage.setOnAction(
                e -> root.setCenter(pagePointage)
        );

        btnParameter.setOnAction(
                e -> root.setCenter(pageParameter)
        );

        /*
         * ROOT
         */

        root.setTop(navbar);

        root.setCenter(pageEmployee);

        /*
         * FENÊTRE
         */

        Dimension dimension =
                Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) dimension.getWidth();

        int height = (int) dimension.getHeight();

        Scene scene = new Scene(
                root,
                width,
                height - 80
        );

        scene.getStylesheets().add(
                getClass()
                        .getResource("style.css")
                        .toExternalForm()
        );

        stage.setTitle("Application");

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}