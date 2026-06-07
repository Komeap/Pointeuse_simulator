package PrincipalApplication;

import Check.Check;
import Check.CheckType;
import Employee.Employee;
import Entreprise.Department;
import Serveur.Server;
import Serialization.Serialization;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrincipalApplicationMMI extends Application {

    private static final String DEPARTMENT_FILE = "departments.ser";

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        /* BACKEND */
        ClockingManager clockingManager = new ClockingManager();

        ObservableList<Department> departments = FXCollections.observableArrayList();

        @SuppressWarnings("unchecked")
        List<Department> loaded = (List<Department>) Serialization.loadObject(DEPARTMENT_FILE);

        if (loaded != null) {
            departments.addAll(loaded);
        }

        EmployeeManager employeeManager = new EmployeeManager(departments);

        Server server = new Server(clockingManager);
        server.start();

        /* NAVBAR */

        Button btnEmployee = new Button("Employee");
        Button btnCheck = new Button("Check");
        Button btnDepartment = new Button("Department");

        btnEmployee.getStyleClass().add("nav-button");
        btnCheck.getStyleClass().add("nav-button");
        btnDepartment.getStyleClass().add("nav-button");

        HBox navbar = new HBox(15, btnEmployee, btnCheck, btnDepartment);
        navbar.getStyleClass().add("navbar");

        /* TABLE EMPLOYEES */

        TableView<Employee> tableEmployee = new TableView<>();

        TableColumn<Employee, String> colEmpId = new TableColumn<>("UUID");
        TableColumn<Employee, String> colFirstName = new TableColumn<>("First Name");
        TableColumn<Employee, String> colLastName = new TableColumn<>("Last Name");
        TableColumn<Employee, String> colDepartment = new TableColumn<>("Department");

        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDepartment.setCellValueFactory(cellData -> {
            Department dep = cellData.getValue().getDepartment();

            String name = (dep == null || dep.getDepartment() == null)
                    ? "N/A"
                    : dep.getDepartment();

            return new javafx.beans.property.SimpleStringProperty(name);
        });

        tableEmployee.getColumns().addAll(colEmpId, colFirstName, colLastName, colDepartment);

        tableEmployee.setItems(employeeManager.getEmployeeList());
        tableEmployee.setPrefHeight(500);
        tableEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAddEmployee = new Button("Add Employee");
        Button btnEditEmployee = new Button("Modify Employee");
        Button btnDeleteEmployee = new Button("Delete Employee");

        HBox employeeActions = new HBox(10, btnAddEmployee, btnEditEmployee, btnDeleteEmployee);

        btnAddEmployee.setOnAction(e -> employeeManager.addEmployee());

        btnEditEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            employeeManager.modifyEmployee(selection);
        });

        btnDeleteEmployee.setOnAction(e -> {
            Employee selection = tableEmployee.getSelectionModel().getSelectedItem();
            employeeManager.deleteEmployee(selection);
        });

        PlanningService planningService = new PlanningService();
        HBox visualScheduleBar = new HBox();
        Label lblScheduleInfo = new Label("Select an employee to display their schedule");
        lblScheduleInfo.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #5C4033;" +
                        "-fx-font-size: 14px;"
        );

        HBox daySelector = new HBox(10);
        ToggleGroup dayGroup = new ToggleGroup();

        java.time.DayOfWeek[] daysOfWeek = {
                java.time.DayOfWeek.MONDAY,
                java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY,
                java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        };

        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 0; i < daysOfWeek.length; i++) {
            ToggleButton btnDay = new ToggleButton(dayNames[i]);
            btnDay.setToggleGroup(dayGroup);
            btnDay.setUserData(daysOfWeek[i]);
            if (i == 0) btnDay.setSelected(true);
            daySelector.getChildren().add(btnDay);
        }

        tableEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateVisualSchedule(tableEmployee, dayGroup, planningService, visualScheduleBar, lblScheduleInfo);
        });

        dayGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dayGroup.selectToggle(oldVal);
            } else {
                updateVisualSchedule(tableEmployee, dayGroup, planningService, visualScheduleBar, lblScheduleInfo);
            }
        });

        Label employeeTitle = new Label("Employees");
        employeeTitle.getStyleClass().add("page-title");

        VBox pageEmployee = new VBox(
                15,
                employeeTitle,
                employeeActions,
                tableEmployee,
                new Separator(),
                daySelector,
                lblScheduleInfo,
                visualScheduleBar
        );

        pageEmployee.setPadding(new Insets(15));

        /* TABLE POINTAGE */

        TableView<Check> tablePointage = new TableView<>();

        TableColumn<Check, String> colCheckFirstName = new TableColumn<>("First Name");
        TableColumn<Check, String> colCheckLastName = new TableColumn<>("Last Name");
        TableColumn<Check, String> colCheckDept = new TableColumn<>("Department");

        TableColumn<Check, LocalDate> colDate = new TableColumn<>("Date");
        TableColumn<Check, LocalTime> colTime = new TableColumn<>("Time");
        TableColumn<Check, CheckType> colType = new TableColumn<>("Type");

        colCheckFirstName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getFirstName() : "Unknown");
        });

        colCheckLastName.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);
            return new javafx.beans.property.SimpleStringProperty(emp != null ? emp.getLastName() : "Unknown");
        });

        colCheckDept.setCellValueFactory(cellData -> {
            UUID empId = cellData.getValue().getEmployeeUUID();
            Employee emp = findEmployeeById(employeeManager.getEmployeeList(), empId);

            return new javafx.beans.property.SimpleStringProperty(
                    (emp != null && emp.getDepartment() != null)
                            ? emp.getDepartment().getDepartment()
                            : "N/A"
            );
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(new PropertyValueFactory<>("checkType"));

        tablePointage.getColumns().addAll(colCheckFirstName, colCheckLastName, colCheckDept, colDate, colTime, colType);

        tablePointage.setItems(clockingManager.getClockingList());

        tablePointage.setRowFactory(tv -> new TableRow<Check>() {

            @Override
            protected void updateItem(Check check, boolean empty) {
                super.updateItem(check, empty);

                if (check == null || empty) {
                    setStyle("");
                    return;
                }

                Employee emp = findEmployeeById(
                        employeeManager.getEmployeeList(),
                        check.getEmployeeUUID()
                );

                if (emp == null || emp.getPlanning() == null) {
                    setStyle("");
                    return;
                }

                java.time.DayOfWeek day = check.getDate().getDayOfWeek();
                Planning.WorkDay workDay = emp.getPlanning().getWorkDay(day);


                if (workDay == null) {
                    setStyle("-fx-background-color: #ff4d4d;"); // rouge
                    return;
                }

                LocalTime start = workDay.getStartTime();
                LocalTime end = workDay.getEndTime();

                LocalTime time = check.getTime();


                if (time.isBefore(start) || time.isAfter(end)) {
                    setStyle("-fx-background-color: #ffb84d;"); // orange
                    return;
                }


                setStyle("");
            }
        });

        tablePointage.setPrefHeight(500);
        tablePointage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnEditCheck = new Button("Modify Check");
        Button btnDeleteCheck = new Button("Delete Check");

        HBox checkActions = new HBox(10, btnEditCheck, btnDeleteCheck);

        btnEditCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            clockingManager.editClocking(selection);
        });

        btnDeleteCheck.setOnAction(e -> {
            Check selection = tablePointage.getSelectionModel().getSelectedItem();
            clockingManager.deleteClocking(selection);
        });

        Label checkTitle = new Label("Check");
        checkTitle.getStyleClass().add("page-title");


        VBox pagePointage = new VBox(
                15,
                checkTitle,
                checkActions,
                tablePointage
        );

        pagePointage.setPadding(new Insets(15));

        /* TABLE DEPARTMENT */

        TableView<Department> tableDepartment = new TableView<>();

        TableColumn<Department, String> colDepName = new TableColumn<>("Department");
        colDepName.setCellValueFactory(new PropertyValueFactory<>("department"));

        tableDepartment.getColumns().add(colDepName);
        tableDepartment.setItems(departments);

        tableDepartment.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAddDepartment = new Button("Add Department");
        Button btnDeleteDepartment = new Button("Delete Department");

        btnAddDepartment.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Department");
            dialog.setContentText("Department name:");

            dialog.showAndWait().ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    Department d = new Department(name);
                    departments.add(d);
                    Serialization.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
                }
            });
        });

        btnDeleteDepartment.setOnAction(e -> {
            Department dep = tableDepartment.getSelectionModel().getSelectedItem();
            if (dep == null) return;

            // unlink employees
            for (Employee emp : employeeManager.getEmployeeList()) {
                if (dep.equals(emp.getDepartment())) {
                    emp.setDepartment(null);
                }
            }

            // remove department
            departments.remove(dep);

            // refresh tables that depend on it
            tableEmployee.refresh();
            tablePointage.refresh();

            Serialization.saveObject(new ArrayList<>(departments), DEPARTMENT_FILE);
        });

        HBox departmentActions = new HBox(10, btnAddDepartment, btnDeleteDepartment);

        Label departmentTitle = new Label("Departments");
        departmentTitle.getStyleClass().add("page-title");
        VBox pageDepartment = new VBox(
                15,
                departmentTitle,
                departmentActions,
                tableDepartment
        );

        pageDepartment.setPadding(new Insets(15));

        /* NAV */

        btnEmployee.setOnAction(e -> root.setCenter(pageEmployee));
        btnCheck.setOnAction(e -> root.setCenter(pagePointage));
        btnDepartment.setOnAction(e -> root.setCenter(pageDepartment));

        root.setTop(navbar);
        root.setCenter(pageEmployee);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        btnAddEmployee.getStyleClass().add("action-button");
        btnEditEmployee.getStyleClass().add("action-button");
        btnDeleteEmployee.getStyleClass().add("action-button");

        btnEditCheck.getStyleClass().add("action-button");
        btnDeleteCheck.getStyleClass().add("action-button");

        btnAddDepartment.getStyleClass().add("action-button");
        btnDeleteDepartment.getStyleClass().add("action-button");

        pageEmployee.getStyleClass().add("page");
        pagePointage.getStyleClass().add("page");
        pageDepartment.getStyleClass().add("page");

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Application");
        stage.show();
    }

    private Employee findEmployeeById(ObservableList<Employee> list, UUID id) {
        if (id == null) return null;
        for (Employee e : list) {
            if (id.equals(e.getEmployeeId())) return e;
        }
        return null;
    }

    public static void main(String[] args) {
        launch();
    }

    private void updateVisualSchedule(TableView<Employee> table, ToggleGroup group, PlanningService service, HBox bar, Label label) {
        Employee selectedEmp = table.getSelectionModel().getSelectedItem();
        ToggleButton selectedDayBtn = (ToggleButton) group.getSelectedToggle();

        if (selectedEmp != null && selectedDayBtn != null) {
            java.time.DayOfWeek day = (java.time.DayOfWeek) selectedDayBtn.getUserData();
            service.loadSchedule(bar, label, selectedEmp, day);
        }
    }
}