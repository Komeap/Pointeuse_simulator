/**
 * This class 'TimeClockMMI' manage the diplay of time clock and the clocking manager.
 * The user can choose his name and send his clocking to the server so that it sends it to the main application.
 *
 */

package TimeClock;

import Check.CheckType;
import Employee.Employee;
import Serveur.Message;
import Serialization.Serialization;
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

public class TimeClockMMI extends Application {

    // - - - ATTRIBUTES - - -
    /**
     * this list stock the checks before send it to the server
     */
    private static List<Message> bufferPointages = Collections.synchronizedList(new ArrayList<>());

    /**
     * this attribute is the IP address of server
     */
    private static String serverIp = "localhost";

    /**
     * this attribute is the server port
     */
    private static int serverPort = 5005;

    /**
     * this attribute is the time that we take for the refresh
     */
    private static int refreshSeconds = 5;

    // - - - SETTERS - - -

    /**
     * it's the setter of server IP
     * @param ip : String
     */
    public static synchronized void setServerIp(String ip) {serverIp = ip;}

    /**
     * it's the setter of server port
     * @param port : int
     */
    public static synchronized void setServerPort(int port)
    {
        //We check that the port in parameter exits
        if (port > 0 && port <= 65535)
            serverPort = port;
    }

    /**
     * it's the setter for the refresh time
     * @param seconds : int
     */
    public static synchronized void setRefreshSeconds(int seconds)
    {
        //We can check that the time it's positive (a negative refresh time is impossible)
        if (seconds > 0)
            refreshSeconds = seconds;
    }

    // - - - GETTER - - -
    /**
     * getter for the refresh time
     * @return int
     */
    public static synchronized int getRefreshSeconds() {return refreshSeconds;}

    /**
     * static block to restore the data in the 'buffer_pointeuse.ser' file and be able to display it afterwards
     */
    static
    {
        @SuppressWarnings("unchecked")
        List<Message> loadBuffer = (List<Message>) Serialization.loadObject("buffer_pointeuse.ser");
        if (loadBuffer != null) //We check that the loadBuffer has been load
        {
            bufferPointages.addAll(loadBuffer);
            System.out.println("clocking restored : " + bufferPointages.size());
        }
    }

    /**
     * main for lunch the javaFx application
     * @param args : String
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * This method initializes and then displays the interface of the time clock emulator in JavaFX
     * So it allows you to configure the time display to have the time of attendance.
     * It updates the files. ser
     * It allows you to configure the information of the server with which it communicates.
     * @param primaryStage : Stage : the primary window for the application
     */
    @Override
    public void start(Stage primaryStage)
    {
        //start the thread who send the information to server.
        startThread();

        primaryStage.setTitle("Emulator Time Clock");

        //display the date and time
        Label labelDate = new Label("loading..."); //the time that the date is displayed
        labelDate.setFont(Font.font("Arial", 20));

        Label labelTime = new Label("loading..."); //the time that the hour is displayed
        labelTime.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        //this is the time rounded to a quarter of an hour
        Label labelRoundHeure = new Label("loading...");
        labelRoundHeure.setFont(Font.font("Arial", 12));

        //the list of employees (dynamic) who can point
        ComboBox<Employee> choiceEmployer = new ComboBox<>();

        //Here, we load the company’s employees using the 'employees.ser' file.
        Runnable refreshEmployees = () -> {
            @SuppressWarnings("unchecked")
            //We load the 'employees.ser' file
            List<Employee> employeeList = (List<Employee>) Serialization.loadObject("employees.ser");
            if (employeeList != null && !employeeList.isEmpty())  //we check that there are employees and it's well load
            {
                //we save the employee selected
                Employee employeeSelected = choiceEmployer.getValue();

                //we update the dynamic list of employee
                choiceEmployer.setItems(FXCollections.observableArrayList(employeeList));

                //we manage the case where there are no selected employees
                if (employeeSelected == null)
                {
                    //so we chose the first employee of the list
                    choiceEmployer.getSelectionModel().selectFirst();
                    return;
                }

                //Here we recup the employee selected by the user, which we will stock in a variable of type 'Employee'.
                //This allows you to keep the employee selected even if there is a refresh. 
                //else, the display returns to the first employer in the list.
                Employee sameEmployee = null;
                for (Employee employeeInProgress : employeeList)
                {
                    //if the id is the same
                    if (employeeInProgress.getEmployeeId() != null && employeeInProgress.getEmployeeId().equals(employeeSelected.getEmployeeId()))
                    {
                        sameEmployee = employeeInProgress;
                        break; //we leave the loop because we found the employer
                    }
                }

                if (sameEmployee != null) //if the employee has been found, he is reassigned his place
                    choiceEmployer.setValue(sameEmployee);
                else //else it’s the first one
                    choiceEmployer.getSelectionModel().selectFirst();
            }
        };

        //start the refresh employee
        refreshEmployees.run();

        //we refresh the employees who are selectable every five seconds
        Timeline autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            refreshEmployees.run();
        }));
        //these commands allow to refresh every five seconds automatically
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();

        Button checkButton = new Button("Check in/out");
        //Button btnRefresh = new Button("-><-");
        Button settingsButton = new Button("⚙");

        //manage the event on the settings button
        settingsButton.setOnAction(event -> {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Server configuration");

            //the input fields of the IP and the port
            TextField ipField = new TextField(serverIp);
            TextField portField = new TextField(String.valueOf(serverPort));

            //grid for the display
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("IP :"), 0, 0);
            grid.add(ipField, 1, 0);

            grid.add(new Label("Port :"), 0, 1);
            grid.add(portField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            //the button for save the changement
            ButtonType saveButton = new ButtonType("Save");
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            Optional<ButtonType> resultChange = dialog.showAndWait();

            if (resultChange.isPresent() && resultChange.get() == saveButton) {
                try { //we try to change the information port and IP with that the user enter
                    setServerIp(ipField.getText());
                    setServerPort(Integer.parseInt(portField.getText()));

                    System.out.println("New configuration : " + serverIp + ":" + serverPort);

                } catch (NumberFormatException error) { //if it's invalid, we manage the error
                    System.out.println("Invalid port.");
                }
            }
        });


        //the center pannel for the time and date display
        VBox timeDisplay = new VBox(10);
        timeDisplay.setAlignment(Pos.CENTER);
        timeDisplay.setPadding(new Insets(20, 0, 0, 0));

        //add of the labels in the display
        timeDisplay.getChildren().addAll(labelDate, labelTime, labelRoundHeure);

        //the top bar with parameter button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT); //in the top right corner
        topBar.setPadding(new Insets(10));
        topBar.getChildren().add(settingsButton); //we add the settings button

        //container in the center of display
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(topBar, timeDisplay);

        //bottom bar with the list and the button check
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(0, 0, 20, 0));
        bottomBar.getChildren().addAll(choiceEmployer, checkButton);

        //principal container with the top and bottom container
        BorderPane root = new BorderPane();
        root.setCenter(topContainer);
        root.setBottom(bottomBar);;

        //manage the event on the check button
        checkButton.setOnAction(event -> {
            Employee selected = choiceEmployer.getValue(); //we recup the employee selected
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
            Employee employe = choiceEmployer.getValue(); //we recup the employee choose
            if (employe != null)
                System.out.println("you have chosen : " + employe.getFirstName() + " " + employe.getLastName());
        });

        // JavaFX Timeline (remplace le javax.swing.Timer) exécuté chaque seconde
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime monHeure = LocalDateTime.now();

            // Heure exacte
            DateTimeFormatter formateurH = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelTime.setText(monHeure.format(formateurH));

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
            Serialization.saveObject(new ArrayList<>(bufferPointages), "buffer_pointeuse.ser");
            System.out.println("Buffer sauvegardé avant fermeture.");
            Platform.exit(); // Arrête proprement JavaFX
            System.exit(0);  // Arrête le processus entier (dont le Thread d'envoi)
        });

        // Création et affichage de la scène
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private static void startThread() {
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

                            // Succès : on retire le message
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

        // Permet au Thread de s'arrêter automatiquement si l'application JavaFX se ferme
        threadEnvoi.setDaemon(true);
        threadEnvoi.start();
    }

    // Getters et Setters
    public static List<Message> getBufferPointages() {
        return bufferPointages;
    }

    public static void setBufferPointages(List<Message> bufferPointages) {
        TimeClockMMI.bufferPointages = bufferPointages;
    }
}