/**
 * The 'EmployeeManager' class manages a list to display your employee in your principal interface.
 * It provides all the necessary functions to add, modify, and delete employees, as well as manage their department
 * assignment and weekly planning, while ensuring data persistence through serialization.
 */
package PrincipalApplication;

import Employee.Employee;
import Entreprise.Department;
import Serialization.Serialization;
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

public class EmployeeManager {

    // - - - ATTRIBUTES - - -
    /** list of employees used for JavaFX display (ObservableList for UI updates) */
    private ObservableList<Employee> employeeList;

    /** List of departments used for selection in UI */
    private ObservableList<Department> departmentList;

    /**
     * file used for serialization (save/load employees)
     * 'final' for that no one modify it
     */
    private static final String fileName = "employees.ser";

    //- - - CONSTRUCTOR - - -

    /**
     * Constructor initializes employee list and loads saved data if available
     * @param departments : List<Department>
     */
    public EmployeeManager(List<Department> departments) {
        this.employeeList = FXCollections.observableArrayList();
        this.departmentList = (ObservableList<Department>) departments;

        //we try to load previously saved employees
        @SuppressWarnings("unchecked")
        List<Employee> loadFile =
                (List<Employee>) Serialization.loadObject(fileName);

        //if data exists, we restore it
        if (loadFile != null && !loadFile.isEmpty()) {
            this.employeeList.addAll(loadFile);
        }
    }

    // - - - GETTER - - -
    /**
     * returns observable list for UI binding
     * @return employeeList
     */
    public ObservableList<Employee> getEmployeeList() {
        return employeeList;
    }

    // - - - METHODS - - -
    /**
     * opens a dialog to create a new employee with schedule configuration
     */
    public void addEmployee() {

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Add an Employee");
        dialog.setHeaderText("Please fill in the employee information and schedule");

        ButtonType validatingButton =
                new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(validatingButton, ButtonType.CANCEL);

        //main employee info form
        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(10);
        gridInfo.setVgap(10);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        ComboBox<Department> deptComboBox = new ComboBox<>();
        deptComboBox.setItems(departmentList);
        deptComboBox.setPromptText("Select a department");

        gridInfo.add(new Label("First Name:"), 0, 0);
        gridInfo.add(firstNameField, 1, 0);
        gridInfo.add(new Label("Last Name:"), 0, 1);
        gridInfo.add(lastNameField, 1, 1);
        gridInfo.add(new Label("Department:"), 0, 2);
        gridInfo.add(deptComboBox, 1, 2);

        //schedule arrays (one per day of week)
        CheckBox[] cbDays = new CheckBox[7];

        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbStart = new ComboBox[7];

        @SuppressWarnings("unchecked")
        ComboBox<LocalTime>[] cbEnd = new ComboBox[7];

        Label lblTotal = new Label();

        //generate default schedule
        Planning.Planning randomBase = randomPlanningGenerator();

        VBox planningBox =
                createSchedulePanel(cbDays, cbStart, cbEnd, lblTotal, randomBase);

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 20, 10, 20));
        mainContainer.getChildren().addAll(gridInfo, new Separator(), planningBox);

        dialog.getDialogPane().setContent(mainContainer);

        //validation before closing dialog
        final Button btOk =
                (Button) dialog.getDialogPane().lookupButton(validatingButton);

        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            //we check required fields
            if (firstNameField.getText().trim().isEmpty()
                    || lastNameField.getText().trim().isEmpty()) {

                showError("Missing Info",
                        "Please enter a valid first and last name.");
                event.consume();
                return;
            }

            //we validate schedule consistency
            if (!validateSchedules(cbDays, cbStart, cbEnd)) {
                event.consume();
            }
        });

        //build employee object from form data
        dialog.setResultConverter(dialogButton -> {

            if (dialogButton == validatingButton) {

                Planning.Planning finalPlanning = new Planning.Planning();
                DayOfWeek[] daysArr = DayOfWeek.values();

                //we build weekly planning
                for (int i = 0; i < 7; i++) {
                    if (cbDays[i].isSelected()) {
                        finalPlanning.setWorkDay(
                                daysArr[i],
                                new Planning.WorkDay(
                                        cbStart[i].getValue(),
                                        cbEnd[i].getValue()
                                )
                        );
                    }
                }

                return new Employee(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        deptComboBox.getValue(),
                        finalPlanning
                );
            }
            return null;
        });

        //add employee to list and save
        Optional<Employee> result = dialog.showAndWait();

        result.ifPresent(employee -> {
            employeeList.add(employee);

            //we link employee to department if needed
            if (employee.getDepartment() != null) {
                employee.getDepartment().addEmployee(employee);
            }

            saveData();
        });
    }

    /**
     * opens a dialog to modify an existing employee
     * @param selectedEmployee : Employee (Employee class)
     */
    public void modifyEmployee(Employee selectedEmployee) {

        //safety check
        if (selectedEmployee == null) {
            showError("Missing Selection",
                    "Please select an employee from the table first.");
            return;
        }

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Modify an Employee");
        dialog.setHeaderText("Modification of: "
                + selectedEmployee.getFirstName() + " "
                + selectedEmployee.getLastName());

        ButtonType validatingButton =
                new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(validatingButton, ButtonType.CANCEL);

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(10);
        gridInfo.setVgap(10);

        TextField firstNameField =
                new TextField(selectedEmployee.getFirstName());

        TextField lastNameField =
                new TextField(selectedEmployee.getLastName());

        ComboBox<Department> deptComboBox =
                new ComboBox<>(departmentList);

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

        VBox planningBox =
                createSchedulePanel(cbDays, cbStart, cbEnd, lblTotal,
                        selectedEmployee.getPlanning());

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 20, 10, 20));
        mainContainer.getChildren().addAll(gridInfo, new Separator(), planningBox);

        dialog.getDialogPane().setContent(mainContainer);

        final Button btOk =
                (Button) dialog.getDialogPane().lookupButton(validatingButton);

        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            if (firstNameField.getText().trim().isEmpty()
                    || lastNameField.getText().trim().isEmpty()) {

                showError("Missing Info",
                        "Please enter a valid first and last name.");
                event.consume();
                return;
            }

            if (!validateSchedules(cbDays, cbStart, cbEnd)) {
                event.consume();
            }
        });

        //update employee data
        dialog.setResultConverter(dialogButton -> {

            if (dialogButton == validatingButton) {

                selectedEmployee.setFirstName(firstNameField.getText());
                selectedEmployee.setLastName(lastNameField.getText());

                Department oldDept = selectedEmployee.getDepartment();
                Department newDept = deptComboBox.getValue();

                //we update department links
                if (oldDept != null && !oldDept.equals(newDept)) {
                    oldDept.removeEmployee(selectedEmployee);
                }

                if (newDept != null && !newDept.equals(oldDept)) {
                    newDept.addEmployee(selectedEmployee);
                }

                selectedEmployee.setDepartment(newDept);

                //we rebuild planning
                Planning.Planning finalPlanning = new Planning.Planning();
                DayOfWeek[] daysArr = DayOfWeek.values();

                for (int i = 0; i < 7; i++) {
                    if (cbDays[i].isSelected()) {
                        finalPlanning.setWorkDay(
                                daysArr[i],
                                new Planning.WorkDay(
                                        cbStart[i].getValue(),
                                        cbEnd[i].getValue()
                                )
                        );
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
            saveData();
        });
    }

    /**
     * delete employee and clean department link
     * @param selectedEmployee : Employee (Employee class)
     */
    public void deleteEmployee(Employee selectedEmployee) {

        if (selectedEmployee == null) {
            showError("Missing Selection",
                    "Please select an employee from the table first.");
            return;
        }

        if (selectedEmployee.getDepartment() != null) {
            selectedEmployee.getDepartment()
                    .removeEmployee(selectedEmployee);
        }

        employeeList.remove(selectedEmployee);
        saveData();
    }

    /**
     * save employee list to file
     */
    private void saveData() {
        Serialization.saveObject(
                new ArrayList<>(employeeList),
                fileName
        );
    }

    /**
     * generate random weekly planning for default values
     * @return planning
     */
    private Planning.Planning randomPlanningGenerator() {

        Planning.Planning planning = new Planning.Planning();

        List<DayOfWeek> days = new ArrayList<>(List.of(DayOfWeek.values()));

        Collections.shuffle(days);

        for (int loop = 0; loop < 5; loop++) {

            int startHour = 8 + (int) (Math.random() * 3);
            LocalTime start = LocalTime.of(startHour, 0);
            LocalTime end = start.plusHours(7);

            planning.setWorkDay(days.get(loop),
                    new Planning.WorkDay(start, end));
        }

        return planning;
    }

    /**
     * creates schedule UI panel for each day of week
     * @param cbDays : CheckBox[]
     * @param cbStart : ComboBox<LocalTime>[]
     * @param cbEnd : ComboBox<LocalTime>[]
     * @param label : Label
     * @param basePlanning : Planning.Planning
     * @return ...
     */
    private VBox createSchedulePanel(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd, Label label, Planning.Planning basePlanning)
    {

        VBox vbox = new VBox(10);
        vbox.getChildren().add(
                new Label("Weekly Schedule Configuration:")
        );

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

            cbDays[i] = new CheckBox(
                    capitalize(currentDay.toString())
            );

            cbStart[i] =
                    new ComboBox<>(FXCollections.observableArrayList(times));

            cbEnd[i] =
                    new ComboBox<>(FXCollections.observableArrayList(times));

            cbStart[i].setDisable(true);
            cbEnd[i].setDisable(true);

            //restore existing planning if available
            if (basePlanning != null &&
                    basePlanning.getWorkDay(currentDay) != null) {

                Planning.WorkDay wd =
                        basePlanning.getWorkDay(currentDay);

                if (wd.getStartTime() != null &&
                        wd.getEndTime() != null) {

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

                if (checked && cbStart[index].getValue() == null)
                    cbStart[index].setValue(LocalTime.of(9, 0));

                if (checked && cbEnd[index].getValue() == null)
                    cbEnd[index].setValue(LocalTime.of(17, 0));

                calculateHourTotal(cbDays, cbStart, cbEnd, label);
            });

            cbStart[i].setOnAction(
                    e -> calculateHourTotal(cbDays, cbStart, cbEnd, label)
            );

            cbEnd[i].setOnAction(
                    e -> calculateHourTotal(cbDays, cbStart, cbEnd, label)
            );

            grid.add(cbDays[i], 0, i);
            grid.add(cbStart[i], 1, i);
            grid.add(new Label("to"), 2, i);
            grid.add(cbEnd[i], 3, i);
        }

        calculateHourTotal(cbDays, cbStart, cbEnd, label);

        vbox.getChildren().addAll(grid, label);
        return vbox;
    }

    /**
     * calculate total weekly working hours
     * @param cbDays : CheckBox[]
     * @param cbStart : ComboBox<LocalTime>[]
     * @param cbEnd : ComboBox<LocalTime>[]
     * @param label : Label label
     */
    private void calculateHourTotal(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd, Label label) {

        int totalMinutes = 0;

        for (int i = 0; i < 7; i++) {

            if (cbDays[i].isSelected()
                    && cbStart[i].getValue() != null
                    && cbEnd[i].getValue() != null) {

                LocalTime start = cbStart[i].getValue();
                LocalTime end = cbEnd[i].getValue();

                int startMins =
                        start.getHour() * 60 + start.getMinute();

                int endMins =
                        end.getHour() * 60 + end.getMinute();

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

        label.setText(String.format("Total scheduled: %dh%02d", hours, minutes));

        label.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
    }

    /**
     * validate that schedule inputs are correct
     * @param cbDays : CheckBox[]
     * @param cbStart : ComboBox<LocalTime>[]
     * @param cbEnd : ComboBox<LocalTime>[]
     * @return booleen : true or false
     */
    private boolean validateSchedules(CheckBox[] cbDays, ComboBox<LocalTime>[] cbStart, ComboBox<LocalTime>[] cbEnd) {

        for (int i = 0; i < 7; i++) {

            if (cbDays[i].isSelected()) {

                LocalTime start = cbStart[i].getValue();
                LocalTime end = cbEnd[i].getValue();

                if (start == null || end == null) {
                    showError("Schedule Error",
                            "Times missing for "
                                    + capitalize(DayOfWeek.values()[i].toString()));
                    return false;
                }

                int startMins =
                        start.getHour() * 60 + start.getMinute();

                int endMins =
                        end.getHour() * 60 + end.getMinute();

                if (endMins == 0) {
                    endMins = 24 * 60;
                }

                if (endMins <= startMins) {
                    showError("Schedule Error",
                            "On "
                                    + capitalize(DayOfWeek.values()[i].toString())
                                    + ", the end time must be after the start time.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * show error alert popup
     * @param title : String
     * @param message : String
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * capitalize first letter of a string (used for days display)
     * @param text : String
     * @return ...
     */
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase()
                + text.substring(1).toLowerCase();
    }
}