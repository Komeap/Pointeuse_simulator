package Serveur;

import Check.CheckType;
import Employee.Employee;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PointeuseIHM extends Application {

    private static List<Message> bufferPointages = Collections.synchronizedList(new ArrayList<>());
    private static String serverIp = "localhost";
    private static int serverPort = 5001;
    private static int refreshSeconds = 5;

    public static synchronized void setServerIp(String ip) {
        serverIp = ip;
    }

    public static synchronized void setServerPort(int port) {
        if (port > 0 && port <= 65535) {
            serverPort = port;
        }
    }

    public static synchronized void setRefreshSeconds(int seconds) {
        if (seconds > 0) {
            refreshSeconds = seconds;
        }
    }

    public static synchronized int getRefreshSeconds() {
        return refreshSeconds;
    }

    // Bloc statique pour charger la sauvegarde au lancement de la classe
    static {
        @SuppressWarnings("unchecked")
        List<Message> charge = (List<Message>) Serialisation.loadObject("buffer_pointeuse.ser");
        if (charge != null) {
            bufferPointages.addAll(charge);
            System.out.println("Pointages restaurés : " + bufferPointages.size());
        }
    }

    public static void main(String[] args) {
        // Lance l'application JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Démarrage du thread d'envoi en arrière-plan
        demarrerThreadEnvoi();

        primaryStage.setTitle("Pointeuse Emulateur");

        // Affichage de la date et de l'heure
        Label labelDate = new Label("Chargement...");
        labelDate.setFont(Font.font("Arial", 20));

        Label labelHeure = new Label("Chargement...");
        labelHeure.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label labelRoundHeure = new Label("");
        labelRoundHeure.setFont(Font.font("Arial", 12));

        //Liste des emplyés dynamique

        ComboBox<Employee> choiceEmployer = new ComboBox<>();

        Runnable refreshEmployees = () ->{
            @SuppressWarnings("unchecked")
            List<Employee> listeChargee = (List<Employee>) Serialisation.loadObject("employees.ser");
            if (listeChargee != null && !listeChargee.isEmpty()) {
                choiceEmployer.setItems(FXCollections.observableArrayList(listeChargee));
                choiceEmployer.getSelectionModel().selectFirst();
            } else {
                System.out.println("Aucun employé trouvé dans le fichier partagé.");
            }
        };

        // Premier chargement au lancement
        refreshEmployees.run();

        Button check = new Button("Check in/out");
        Button btnRefresh = new Button("-><-");
        Button btnSettings = new Button("⚙");

        btnSettings.setOnAction(e -> {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Configuration Serveur");

            // Champs
            TextField ipField = new TextField(serverIp);
            TextField portField = new TextField(String.valueOf(serverPort));

            // Layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("IP :"), 0, 0);
            grid.add(ipField, 1, 0);

            grid.add(new Label("Port :"), 0, 1);
            grid.add(portField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            ButtonType saveButton = new ButtonType("Sauvegarder");
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButton) {
                try {
                    setServerIp(ipField.getText());
                    setServerPort(Integer.parseInt(portField.getText()));

                    System.out.println("Nouvelle configuration : " + serverIp + ":" + serverPort);

                } catch (NumberFormatException ex) {
                    System.out.println("Port invalide.");
                }
            }
        });

        btnRefresh.setOnAction(e -> refreshEmployees.run());

        // Panneau central (Temps) avec un VBox
        VBox panneauTemps = new VBox(10);
        panneauTemps.setAlignment(Pos.CENTER);
        panneauTemps.setPadding(new Insets(20, 0, 0, 0));

        // AJOUT DES LABELS
        Label labelEmpty = new Label();
        panneauTemps.getChildren().addAll(labelDate, labelHeure, labelRoundHeure);

        // Barre du haut avec le bouton paramètres
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.getChildren().add(btnSettings);

        // Conteneur principal haut + centre
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(topBar, panneauTemps);

        // Panneau du bas (Contrôles)
        HBox panneauControles = new HBox(15);
        panneauControles.setAlignment(Pos.CENTER);
        panneauControles.setPadding(new Insets(0, 0, 20, 0));
        panneauControles.getChildren().addAll(choiceEmployer, check, btnRefresh);

        // Disposition principale
        BorderPane root = new BorderPane();
        root.setCenter(topContainer);
        root.setBottom(panneauControles);;

        // Événement sur le bouton
        check.setOnAction(e -> {
            Employee selected = choiceEmployer.getValue();
            if (selected != null) {
                UUID idUnique = selected.getEmployeeId();

                LocalDateTime now = LocalDateTime.now();
                int modulo = now.getMinute() % 15;
                int minutesToAdd = (modulo < 8) ? -modulo : (15 - modulo);
                LocalDateTime roundedTime = now.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);

                Message msg = new Message(idUnique, CheckType.OUT, roundedTime);
                bufferPointages.add(msg);
                System.out.println("Pointage enregistré pour " + selected.getFirstName());
            }
        });

        // Événement sur la liste déroulante
        choiceEmployer.setOnAction(e -> {
            Employee employe = choiceEmployer.getValue();
            if (employe != null) {
                System.out.println("Vous avez sélectionné : " + employe.getFirstName() + " " + employe.getLastName());
            }
        });

        // JavaFX Timeline (remplace le javax.swing.Timer) exécuté chaque seconde
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime monHeure = LocalDateTime.now();

            // Heure exacte
            DateTimeFormatter formateurH = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelHeure.setText(monHeure.format(formateurH));

            // Date exacte
            DateTimeFormatter formateur = DateTimeFormatter.ofPattern("EEEE d MMMM, yyyy", Locale.FRENCH);
            labelDate.setText(monHeure.format(formateur));

            // Heure arrondie au quart d'heure
            int modulo = monHeure.getMinute() % 15;
            int minutesToAdd = (modulo < 8) ? -modulo : (15 - modulo);
            LocalDateTime roundedHour = monHeure.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);
            DateTimeFormatter formateurHR = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelRoundHeure.setText("Comptabilisé à l'heure : " + roundedHour.format(formateurHR));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // 2. Sauvegarder automatiquement à la fermeture de la fenêtre
        primaryStage.setOnCloseRequest(e -> {
            Serialisation.saveObject(new ArrayList<>(bufferPointages), "buffer_pointeuse.ser");
            System.out.println("Buffer sauvegardé avant fermeture.");
            Platform.exit(); // Arrête proprement JavaFX
            System.exit(0);  // Arrête le processus entier (dont le Thread d'envoi)
        });

        // Création et affichage de la scène
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void demarrerThreadEnvoi() {
        Thread threadEnvoi = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(getRefreshSeconds() * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if (!bufferPointages.isEmpty()) {
                    System.out.println("Tentative d'envoi... (" + bufferPointages.size() + " message(s) en attente)");

                    while (!bufferPointages.isEmpty()) {
                        Message messageAEnvoyer = bufferPointages.get(0);
                        try (Socket socket = new Socket(serverIp, serverPort);
                             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

                            oos.writeObject(messageAEnvoyer);
                            oos.flush();

                            bufferPointages.remove(0);
                            System.out.println("Message envoyé au serveur !");

                        } catch (Exception ex) {
                            System.out.println("Server injoignable. Fin de la tentative, on réessayera au prochain cycle.");
                            break;
                        }
                    }
                }
            }
        });

        threadEnvoi.setDaemon(true);
        threadEnvoi.start();
    }

    // Getters et Setters
    public static List<Message> getBufferPointages() {
        return bufferPointages;
    }

    public static void setBufferPointages(List<Message> bufferPointages) {
        PointeuseIHM.bufferPointages = bufferPointages;
    }
}