package AppliCationPrincipale;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.Toolkit;

public class PrincipalIHM extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Button btnEmployee = new Button("Gestion des employées");
        Button btnPointage = new Button("Gestion des pointages");
        Button btnParameter = new Button("Paramètres");

        btnEmployee.getStyleClass().add("nav-button");
        btnPointage.getStyleClass().add("nav-button");
        btnParameter.getStyleClass().add("nav-button");

        HBox navbar = new HBox(btnEmployee, btnPointage, btnParameter);
        navbar.getStyleClass().add("navbar");

        VBox pageEmployee = new VBox(new Label("Employés"));
        VBox pagePointage = new VBox(new Label("Pointage"));
        VBox pageParameter = new VBox(new Label("Paramètres"));

        pageEmployee.getStyleClass().add("page2");
        pagePointage.getStyleClass().add("page");
        pageParameter.getStyleClass().add("page");

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnPointage.setOnAction(e -> root.setCenter(pagePointage));
        btnParameter.setOnAction(e -> root.setCenter(pageParameter));

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) dimension.getWidth();
        int height = (int) dimension.getHeight();

        Scene scene = new Scene(root, width, height - 80);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}