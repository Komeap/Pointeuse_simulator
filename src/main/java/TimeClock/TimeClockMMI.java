package TimeClock;

import Check.CheckType;
import Configuration.TimeClockConfig;
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

/**
 * This class 'TimeClockMMI' manage the diplay of time clock and the clocking manager.
 * The user can choose his name and send his clocking to the server so that it sends it to the main application.
 *
 */
public class TimeClockMMI extends Application {

    // - - - ATTRIBUTES - - -
    /**
     * this list stock the checks before send it to the server
     */
    private static List<Message> clockingBuffer = Collections.synchronizedList(new ArrayList<>());

    /**
     * this attribute is the IP address of server
     */
    private static String serverIp;

    /**
     * this attribute is the server port
     */
    private static int serverPort;

    /**
     * this attribute is the time that we take for the refresh
     */
    private static int refreshSeconds;

    /**
     * Configuration file shared with the main application.
     */
    private static final String CONFIG_FILE = "timeclock_config.ser";

    /**
     * the server folder, where the timeclock will pull the config if it exists
     */
    private static String sharedDirPath = "." + java.io.File.separator;

    /**
     * the auth token for "secure" TCP communication
     * this will be developped further in the report but a hard-coded string
     * is a good compromise when the app is intended for the staff
     *
     * SSL implementation would be better but we don't have enough time for that
     * plus it's a little overkill
     */
    private static final String AUTH_TOKEN = "Viv3P0LYTECH2026!!!";
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

    /**
     * it's the setter for the clocking buffer
     * @param clockingBuffer : List Message
     */
    public static void setClockingBuffer(List<Message> clockingBuffer) { TimeClockMMI.clockingBuffer = clockingBuffer; }

    // - - - GETTER - - -
    /**
     * getter for the refresh time
     * @return int
     */
    public static synchronized int getRefreshSeconds() {return refreshSeconds;}

    /**
     * return the buffer oh the clocking
     * @return clockingBuffer
     */
    public static List<Message> getClockingBuffer() { return clockingBuffer; }

    // - - - STATIC BLOCK - - -
    /**
     * static block to restore the data in the 'buffer_pointeuse.ser' file and be able to display it afterwards
     */
    static
    {
        @SuppressWarnings("unchecked")
        List<Message> loadBuffer = (List<Message>) Serialization.loadObject("buffer_pointeuse.ser");
        if (loadBuffer != null) //We check that the loadBuffer has been load
        {
            clockingBuffer.addAll(loadBuffer);
            System.out.println("clocking restored : " + clockingBuffer.size());
        }
    }

    // - - - METHODS - - -
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
        try {
            // try to open server_folder.txt
            java.nio.file.Path folderConfigPath = java.nio.file.Paths.get("server_folder.txt");
            if (java.nio.file.Files.exists(folderConfigPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(folderConfigPath); // if it's valid, read
                if (!lines.isEmpty() && !lines.get(0).trim().isEmpty()) { // +-parsing
                    sharedDirPath = lines.get(0).trim();
                    if (!sharedDirPath.endsWith(java.io.File.separator)) {
                        sharedDirPath += java.io.File.separator;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("can't read server_folder.txt, using default folder");
        }

        // getting local config
        TimeClockConfig config = (TimeClockConfig) Serialization.loadObject(CONFIG_FILE);

        // if no config exists, we create one
        if (config == null) {
            config = new TimeClockConfig(UUID.randomUUID(), "Pointeuse Entrée", "localhost", 5005, 5);
            Serialization.saveObject(config, CONFIG_FILE);
        }

        // we read the main server file to see if it's been modified
        @SuppressWarnings("unchecked")
        List<TimeClockConfig> serverPointeuseList = (List<TimeClockConfig>) Serialization.loadObject(sharedDirPath + "liste_pointeuses.ser");

        if (serverPointeuseList != null) {
            for (TimeClockConfig serverConfig : serverPointeuseList) {
                // if our uuid is on the list, we erase our config and replace it
                if (serverConfig.getId().equals(config.getId())) {
                    config = serverConfig;
                    Serialization.saveObject(config, CONFIG_FILE);
                    System.out.println("Configuration synchronisée depuis le serveur au démarrage !");
                    break;
                }
            }
        }

        // applying config
        final UUID myPointeuseId = config.getId();
        serverIp = config.getIp();
        serverPort = config.getPort();
        refreshSeconds = config.getRefreshSeconds();

        // starting background tasks
        startConfigWatcher();
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
            List<Employee> employeeList = (List<Employee>) Serialization.loadObject(sharedDirPath + "employees.ser");
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





        //the center pannel for the time and date display
        VBox timeDisplay = new VBox(10);
        timeDisplay.setAlignment(Pos.CENTER);
        timeDisplay.setPadding(new Insets(20, 0, 0, 0));

        //add of the labels in the display
        timeDisplay.getChildren().addAll(labelDate, labelTime, labelRoundHeure);


        //bottom bar with the list and the button check
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(0, 0, 20, 0));
        bottomBar.getChildren().addAll(choiceEmployer, checkButton);

        //principal container with the top and bottom container
        BorderPane root = new BorderPane();
        root.setCenter(timeDisplay);
        root.setBottom(bottomBar);

        Button configFolderButton = new Button("Configure server folder");
        configFolderButton.setOnAction(event -> {
            javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
            directoryChooser.setTitle("Choose the shared server folder:");

            // we open the selector where the app is
            java.io.File currentFolder = new java.io.File(sharedDirPath);
            if (currentFolder.exists() && currentFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentFolder);
            }

            // dialog box display
            java.io.File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                String pathStr = selectedDirectory.getAbsolutePath();
                if (!pathStr.endsWith(java.io.File.separator)) {
                    pathStr += java.io.File.separator;
                }
                sharedDirPath = pathStr;

                // local save in the text file for the next startup
                try {
                    java.nio.file.Files.write(
                            java.nio.file.Paths.get("server_folder.txt"),
                            java.util.Collections.singletonList(sharedDirPath)
                    );

                    // ok ! notification
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Config saved !");
                    alert.setHeaderText(null);
                    alert.setContentText("Server folder configured :\n" + sharedDirPath + "\n\nPlease restart the app to apply changes.");
                    alert.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // button on top right
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(configFolderButton);
        root.setTop(topBar);

        //manage the event on the check button
        checkButton.setOnAction(event -> {
            Employee employeeSelect = choiceEmployer.getValue(); //we recup the employee selected
            //we check that teh employee selected is not null
            if (employeeSelect != null)
            {

                UUID employeeSelectId = employeeSelect.getEmployeeId();

                LocalDateTime timeNow = LocalDateTime.now(); //we recup the time

                //we round up the time to a quarter of an hour
                int moduloTime = timeNow.getMinute() % 15;
                int minutesToAdd = (moduloTime < 8) ? -moduloTime : (15 - moduloTime);
                LocalDateTime roundTime = timeNow.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);

                //we create a message with the class 'Message' for save the check and send to the server later
                Message messageCheck = new Message(employeeSelectId, CheckType.OUT, roundTime, AUTH_TOKEN, myPointeuseId);
                clockingBuffer.add(messageCheck); //add to the buffer

                System.out.println("save clocking for " + employeeSelect.getFirstName());
            }
        });

        //manage the event on the dropdown list
        choiceEmployer.setOnAction(event -> {
            Employee employe = choiceEmployer.getValue(); //we recup the employee choose
            if (employe != null) //if not null, we prevent the user in the terminal for his choice
                System.out.println("you have chosen : " + employe.getFirstName() + " " + employe.getLastName());
        });

        //manage the event for update the display for the time round to a quarter of an hour
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {

            LocalDateTime timeNow = LocalDateTime.now(); //we recup the time

            //time formatter hh:mm:ss
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelTime.setText(timeNow.format(timeFormatter));

            //exact date
            DateTimeFormatter exactDate = DateTimeFormatter.ofPattern("EEEE d MMMM, yyyy", Locale.FRENCH);
            labelDate.setText(timeNow.format(exactDate));

            //we round up the time to a quarter of an hour
            int moduloTime = timeNow.getMinute() % 15;
            int minutesToAdd = (moduloTime < 8) ? -moduloTime : (15 - moduloTime);
            LocalDateTime roundHour = timeNow.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES);

            //we formate the time to a quarter of an hour
            DateTimeFormatter dateFormatterRoundQuarter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH);
            labelRoundHeure.setText("the time at quarter of an hour is  " + roundHour.format(dateFormatterRoundQuarter));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //Graceful serialization saving when user closes window frame
        primaryStage.setOnCloseRequest(e -> {
            Serialization.saveObject(new ArrayList<>(clockingBuffer), "buffer_pointeuse.ser");
            System.out.println("Buffer sauvegardé avant fermeture.");
            Platform.exit(); //stop JavaFx
            System.exit(0); //stop all of the processus
        });

        //Create and display the final scene
        Scene finalScene = new Scene(root, 400, 300);
        primaryStage.setScene(finalScene);
        primaryStage.show(); //we display
    }

    /**
     * This method monitors the configuration file and automatically reloads it
     * whenever it is modified.
     * It uses a WatchService running in a background daemon thread to detect
     * changes on the configuration file and apply the new settings without
     * restarting the application.
     */
    private void startConfigWatcher() {
        Thread watcher = new Thread(() -> {
            try {
                //
                java.nio.file.Path path = java.nio.file.Paths.get(CONFIG_FILE);
                java.nio.file.Path dir = path.getParent() != null ? path.getParent() : java.nio.file.Paths.get(".");

                java.nio.file.WatchService watchService = java.nio.file.FileSystems.getDefault().newWatchService();

                dir.register(
                        watchService,
                        java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
                );

                while (true) {
                    java.nio.file.WatchKey key = watchService.take();

                    for (java.nio.file.WatchEvent<?> event : key.pollEvents()) {
                        java.nio.file.WatchEvent.Kind<?> kind = event.kind();

                        if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY) {
                            String fileName = event.context().toString();

                            if (fileName.equals(CONFIG_FILE)) {
                                System.out.println("confi modified, reloading...");

                                TimeClockConfig newConfig =
                                        (TimeClockConfig) Serialization.loadObject(CONFIG_FILE);

                                if (newConfig != null) {
                                    applyConfig(newConfig);
                                }
                            }
                        }
                    }

                    key.reset();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        watcher.setDaemon(true);
        watcher.start();
    }

    /**
     * This method applies the new configuration loaded from the configuration file.
     * It updates the server IP address, the server port and the refresh interval
     * used by the time clock.
     *
     * @param config : TimeClockConfig : the new configuration to apply
     */
    private void applyConfig(TimeClockConfig config) {
        setServerIp(config.getIp());
        setServerPort(config.getPort());
        setRefreshSeconds(config.getRefreshSeconds());

        System.out.println("new config apply : " + config.getIp() + ":" + config.getPort() + " refresh=" + config.getRefreshSeconds());
    }

    /**
     * run the server for exchange the data between the time clock and the principal application.
     * They will exchange the check for the principal application display and manage it
     */
    private static void startThread() {
        //we create a thread for exchange data without the principal application
        Thread threadEnvoi = new Thread(() -> {
            //while the program turn, the server turn also
            while (true)
            {
                try
                {
                    Thread.sleep(getRefreshSeconds() * 1000L); //
                } catch (InterruptedException error) { //we manage the potentials errors
                    error.printStackTrace();
                    break;
                }

                //if the buffer of clocking contains clocking not sent, we send them
                if (!clockingBuffer.isEmpty()) {
                    System.out.println("attempt to send (There are " + clockingBuffer.size() + " message(s) waiting)");

                    //while the buffer isn't empty, we send the clocking to the principal application
                    while (!clockingBuffer.isEmpty()) {
                        Message messageToSend = clockingBuffer.get(0); //we recup the first message (check) of the list
                        //try to open a server connection
                        try (Socket socket = new Socket(serverIp, serverPort); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()))
                        {

                            oos.writeObject(messageToSend); //send the Check in the network with the server
                            oos.flush(); //force sends it (just in case)

                            //we remove the first element because he has been sent
                            clockingBuffer.remove(0);
                            System.out.println("Message sent to the server");

                        } catch (Exception error) {
                            System.out.println("server unreachable. We will try it again in the next try");
                            break;
                        }
                    }
                }
            }
        });

        //Allows to the Thread to automatically stop if the JavaFX application closes
        threadEnvoi.setDaemon(true);
        threadEnvoi.start();
    }
}