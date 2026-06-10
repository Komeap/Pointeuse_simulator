package PrincipalApplication;

import Configuration.TimeClockConfig;
import Serialization.Serialization;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TimeClockManager {
    private ObservableList<TimeClockConfig> pointeuseList;
    private static final String FILE_NAME = "liste_pointeuses.ser";

    public TimeClockManager() {
        pointeuseList = FXCollections.observableArrayList();
        @SuppressWarnings("unchecked")
        List<TimeClockConfig> loaded = (List<TimeClockConfig>) Serialization.loadObject(FILE_NAME);
        if (loaded != null) {
            pointeuseList.addAll(loaded);
        }
    }

    public ObservableList<TimeClockConfig> getPointeuseList() {
        return pointeuseList;
    }

    // Ajoute la pointeuse si c'est la première fois qu'elle communique
    public synchronized void registerPointeuseIfNotExists(UUID id) {
        boolean exists = pointeuseList.stream().anyMatch(p -> p.getId().equals(id));
        if (!exists) {
            TimeClockConfig nouvelle = new TimeClockConfig(id, "Nouvelle Pointeuse", "localhost", 5005, 5);
            Platform.runLater(() -> pointeuseList.add(nouvelle));
            saveData();
        }
    }

    public void saveData() {
        Serialization.saveObject(new ArrayList<>(pointeuseList), FILE_NAME);
    }
}