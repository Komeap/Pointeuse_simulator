package AppliCationPrincipale;

import Employee.Employee;
import Entreprise.Department;
import Serveur.Serialisation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GestionEmployee {
    private ObservableList<Employee> employeeList;
    private ObservableList<Department> departmentList;
    private static final String fileName = "employees.ser";

    public GestionEmployee(List<Department> departments) {
        this.employeeList = FXCollections.observableArrayList();
        this.departmentList = FXCollections.observableArrayList(departments);

        @SuppressWarnings("unchecked")
        List<Employee> loadFile = (List<Employee>) Serialisation.loadObject(fileName);
        if (loadFile != null && !loadFile.isEmpty()) {
            this.employeeList.addAll(loadFile);
        }
    }

    public ObservableList<Employee> getEmployeeList() {
        return employeeList;
    }

    public void ajouterEmployee() {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Add an Employee");
        dialog.setHeaderText("Please fill in the employee information and schedule");

        ButtonType btnValider = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(10);
        gridInfo.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        ComboBox<Department> deptComboBox = new ComboBox<>(departmentList);
        deptComboBox.setPromptText("Select a department");

        gridInfo.add(new Label("First Name:"), 0, 0);
        gridInfo.add(firstNameField, 1, 0);
        gridInfo.add(new Label("Last Name:"), 0, 1);
        gridInfo.add(lastNameField, 1, 1);
        gridInfo.add(new Label("Department:"), 0, 2);
        gridInfo.add(deptComboBox, 1, 2);

        CheckBox[] cbDays = new CheckBox[7];
        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbStart = new ComboBox[7];
        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbEnd = new ComboBox[7];
        Label lblTotal = new Label();

        Planning.Planning randomBase = genererPlanningAleatoire();
        VBox planningBox = creerPanneauPlanning(cbDays, cbStart, cbEnd, lblTotal, randomBase);

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 20, 10, 20));
        mainContainer.getChildren().addAll(gridInfo, new Separator(), planningBox);

        dialog.getDialogPane().setContent(mainContainer);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(btnValider);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                afficherErreur("Missing Info", "Please enter a valid first and last name.");
                event.consume();
                return;
            }
            if (!validerHoraires(cbDays, cbStart, cbEnd)) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                Planning.Planning finalPlanning = new Planning.Planning();
                DayOfWeek[] daysArr = DayOfWeek.values();
                for (int i = 0; i < 7; i++) {
                    if (cbDays[i].isSelected()) {
                        finalPlanning.setWorkDay(daysArr[i], new Planning.WorkDay(cbStart[i].getValue(), cbEnd[i].getValue()));
                    }
                }
                return new Employee(firstNameField.getText(), lastNameField.getText(), deptComboBox.getValue(), finalPlanning);
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            employeeList.add(employee);
            if (employee.getDepartment() != null) {
                employee.getDepartment().addEmployee(employee);
            }
            sauvegarderDonnees();
        });
    }

    public void modifierEmployee(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            afficherErreur("Missing Selection", "Please select an employee from the table first.");
            return;
        }

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Modify an Employee");
        dialog.setHeaderText("Modification of: " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());

        ButtonType btnValider = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(10);
        gridInfo.setVgap(10);

        TextField firstNameField = new TextField(selectedEmployee.getFirstName());
        TextField lastNameField = new TextField(selectedEmployee.getLastName());
        ComboBox<Department> deptComboBox = new ComboBox<>(departmentList);
        deptComboBox.setValue(selectedEmployee.getDepartment());

        gridInfo.add(new Label("First Name:"), 0, 0);
        gridInfo.add(firstNameField, 1, 0);
        gridInfo.add(new Label("Last Name:"), 0, 1);
        gridInfo.add(lastNameField, 1, 1);
        gridInfo.add(new Label("Department:"), 0, 2);
        gridInfo.add(deptComboBox, 1, 2);

        CheckBox[] cbDays = new CheckBox[7];
        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbStart = new ComboBox[7];
        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbEnd = new ComboBox[7];
        Label lblTotal = new Label();

        VBox planningBox = creerPanneauPlanning(cbDays, cbStart, cbEnd, lblTotal, selectedEmployee.getPlanning());

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 20, 10, 20));
        mainContainer.getChildren().addAll(gridInfo, new Separator(), planningBox);

        dialog.getDialogPane().setContent(mainContainer);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(btnValider);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                afficherErreur("Missing Info", "Please enter a valid first and last name.");
                event.consume();
                return;
            }
            if (!validerHoraires(cbDays, cbStart, cbEnd)) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                selectedEmployee.setFirstName(firstNameField.getText());
                selectedEmployee.setLastName(lastNameField.getText());

                Department ancienDept = selectedEmployee.getDepartment();
                Department nouveauDept = deptComboBox.getValue();

                if (ancienDept != null && !ancienDept.equals(nouveauDept)) {
                    ancienDept.removeEmployee(selectedEmployee);
                }
                if (nouveauDept != null && !nouveauDept.equals(ancienDept)) {
                    nouveauDept.addEmployee(selectedEmployee);
                }
                selectedEmployee.setDepartment(nouveauDept);

                Planning.Planning finalPlanning = new Planning.Planning();
                DayOfWeek[] daysArr = DayOfWeek.values();
                for (int i = 0; i < 7; i++) {
                    if (cbDays[i].isSelected()) {
                        finalPlanning.setWorkDay(daysArr[i], new Planning.WorkDay(cbStart[i].getValue(), cbEnd[i].getValue()));
                    }
                }
                selectedEmployee.setPlanning(finalPlanning);

                return selectedEmployee;
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            int index = employeeList.indexOf(employee);
            employeeList.set(index, employee);
            sauvegarderDonnees();
        });
    }

    public void supprimerEmployee(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            afficherErreur("Missing Selection", "Please select an employee from the table first.");
            return;
        }
        if (selectedEmployee.getDepartment() != null) {
            selectedEmployee.getDepartment().removeEmployee(selectedEmployee);
        }
        employeeList.remove(selectedEmployee);
        sauvegarderDonnees();
    }

    private void sauvegarderDonnees() {
        Serialisation.saveObject(new ArrayList<>(employeeList), fileName);
    }

    private Planning.Planning genererPlanningAleatoire() {
        Planning.Planning p = new Planning.Planning();
        List<DayOfWeek> days = new ArrayList<>(List.of(DayOfWeek.values()));
        Collections.shuffle(days);

        for (int i = 0; i < 5; i++) {
            int startHour = 8 + (int) (Math.random() * 3);
            LocalTime start = LocalTime.of(startHour, 0);
            LocalTime end = start.plusHours(7);
            p.setWorkDay(days.get(i), new Planning.WorkDay(start, end));
        }
        return p;
    }

    private VBox creerPanneauPlanning(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd, Label lblTotal, Planning.Planning basePlanning) {
        VBox vbox = new VBox(10);
        vbox.getChildren().add(new Label("Weekly Schedule Configuration:"));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);

        DayOfWeek[] daysArr = DayOfWeek.values();
        List<LocalTime> times = new ArrayList<>();

        for (int h = 0; h <= 23; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 15));
            times.add(LocalTime.of(h, 30));
            times.add(LocalTime.of(h, 45));
        }

        for (int i = 0; i < 7; i++) {
            DayOfWeek currentDay = daysArr[i];
            cbDays[i] = new CheckBox(capitalize(currentDay.toString()));
            cbStart[i] = new ComboBox<>(FXCollections.observableArrayList(times));
            cbEnd[i] = new ComboBox<>(FXCollections.observableArrayList(times));

            cbStart[i].setDisable(true);
            cbEnd[i].setDisable(true);

            if (basePlanning != null && basePlanning.getWorkDay(currentDay) != null) {
                Planning.WorkDay wd = basePlanning.getWorkDay(currentDay);
                if (wd.getStartTime() != null && wd.getEndTime() != null) {
                    cbDays[i].setSelected(true);
                    cbStart[i].setValue(wd.getStartTime());
                    cbEnd[i].setValue(wd.getEndTime());
                    cbStart[i].setDisable(false);
                    cbEnd[i].setDisable(false);
                }
            }

            final int index = i;
            cbDays[i].setOnAction(e -> {
                boolean checked = cbDays[index].isSelected();
                cbStart[index].setDisable(!checked);
                cbEnd[index].setDisable(!checked);
                if (checked && cbStart[index].getValue() == null) cbStart[index].setValue(LocalTime.of(9, 0));
                if (checked && cbEnd[index].getValue() == null) cbEnd[index].setValue(LocalTime.of(17, 0));
                calculerTotalHeures(cbDays, cbStart, cbEnd, lblTotal);
            });

            cbStart[i].setOnAction(e -> calculerTotalHeures(cbDays, cbStart, cbEnd, lblTotal));
            cbEnd[i].setOnAction(e -> calculerTotalHeures(cbDays, cbStart, cbEnd, lblTotal));

            grid.add(cbDays[i], 0, i);
            grid.add(cbStart[i], 1, i);
            grid.add(new Label("to"), 2, i);
            grid.add(cbEnd[i], 3, i);
        }

        calculerTotalHeures(cbDays, cbStart, cbEnd, lblTotal);
        vbox.getChildren().addAll(grid, lblTotal);
        return vbox;
    }

    private void calculerTotalHeures(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd, Label lblTotal) {
        int totalMinutes = 0;

        for (int i = 0; i < 7; i++) {
            if (cbDays[i].isSelected() && cbStart[i].getValue() != null && cbEnd[i].getValue() != null) {
                LocalTime start = cbStart[i].getValue();
                LocalTime end = cbEnd[i].getValue();

                int startMins = start.getHour() * 60 + start.getMinute();
                int endMins = end.getHour() * 60 + end.getMinute();

                if (endMins == 0) {
                    endMins = 24 * 60;
                }

                if (endMins > startMins) {
                    totalMinutes += (endMins - startMins);
                }
            }
        }

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        lblTotal.setText(String.format("Total scheduled: %dh%02d", hours, minutes));
        lblTotal.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
    }

    private boolean validerHoraires(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd) {
        for (int i = 0; i < 7; i++) {
            if (cbDays[i].isSelected()) {
                LocalTime start = cbStart[i].getValue();
                LocalTime end = cbEnd[i].getValue();

                if (start == null || end == null) {
                    afficherErreur("Schedule Error", "Times missing for " + capitalize(DayOfWeek.values()[i].toString()) + ".");
                    return false;
                }

                int startMins = start.getHour() * 60 + start.getMinute();
                int endMins = end.getHour() * 60 + end.getMinute();

                if (endMins == 0) {
                    endMins = 24 * 60;
                }

                if (endMins <= startMins) {
                    afficherErreur("Schedule Error", "On " + capitalize(DayOfWeek.values()[i].toString()) + ", the end time must be after the start time.");
                    return false;
                }
            }
        }
        return true;
    }

    private void afficherErreur(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}