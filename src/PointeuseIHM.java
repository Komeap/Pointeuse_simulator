import Check.CheckType;
/*--module-path
"C:\Users\bess7\Downloads\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1\lib"
--add-modules
javafx.controls,javafx.graphics
--enable-native-access=javafx.graphics*/
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PointeuseIHM extends Application {

    private static List<Message> bufferPointages = Collections.synchronizedList(new ArrayList<>());

    // Bloc statique pour charger la sauvegarde au lancement de la classe
    static {
        @SuppressWarnings("unchecked")
        List<Message> charge = (List<Message>) testSerialisation.loadObject("buffer_pointeuse.ser");
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

        // Liste déroulante des employés
        ComboBox<String> choixEmployer = new ComboBox<>(FXCollections.observableArrayList(
                "Pierre Cointre", "Tiago Espitalier", "Julien Toulzac"
        ));
        choixEmployer.getSelectionModel().selectFirst(); // Sélectionne le premier par défaut

        Button check = new Button("Check in/out");

        // Panneau central (Temps) avec un VBox
        VBox panneauTemps = new VBox(10); // Espacement de 10px entre les éléments
        panneauTemps.setAlignment(Pos.CENTER);
        panneauTemps.setPadding(new Insets(20, 0, 0, 0));
        panneauTemps.getChildren().addAll(labelDate, labelHeure, labelRoundHeure);

        // Panneau du bas (Contrôles) avec un HBox
        HBox panneauControles = new HBox(15); // Espacement de 15px entre les éléments
        panneauControles.setAlignment(Pos.CENTER);
        panneauControles.setPadding(new Insets(0, 0, 20, 0));
        panneauControles.getChildren().addAll(choixEmployer, check);

        // Disposition principale
        BorderPane root = new BorderPane();
        root.setCenter(panneauTemps);
        root.setBottom(panneauControles);

        // Événement sur le bouton
        check.setOnAction(e -> {
            UUID testid = UUID.randomUUID();
            CheckType testcheck = CheckType.OUT;
            Message msg = new Message(testid, testcheck, LocalDateTime.now());

            bufferPointages.add(msg);
            System.out.println("Pointage mis en attente. Total dans le buffer : " + bufferPointages.size());
        });

        // Événement sur la liste déroulante
        choixEmployer.setOnAction(e -> {
            String employe = choixEmployer.getValue();
            System.out.println("Vous avez sélectionné : " + employe);
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
            testSerialisation.saveObject(new ArrayList<>(bufferPointages), "buffer_pointeuse.ser");
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
                    Thread.sleep(5000); // 5 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break; // Sécurité pour quitter le thread si interrompu
                }

                if (!bufferPointages.isEmpty()) {
                    System.out.println("Tentative d'envoi... (" + bufferPointages.size() + " message(s) en attente)");

                    while (!bufferPointages.isEmpty()) {
                        Message messageAEnvoyer = bufferPointages.get(0);
                        try (Socket socket = new Socket("localhost", 5000);
                             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

                            oos.writeObject(messageAEnvoyer);
                            oos.flush();

                            // Succès : on retire le message
                            bufferPointages.remove(0);
                            System.out.println("Message envoyé au serveur !");

                        } catch (Exception ex) {
                            System.out.println("Serveur injoignable. Fin de la tentative, on réessayera au prochain cycle.");
                            break; // On sort de la boucle interne pour patienter à nouveau 5 secondes
                        }
                    }
                }
            }
        });

        // Permet au Thread de s'arrêter automatiquement si l'application JavaFX se ferme
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