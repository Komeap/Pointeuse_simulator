package Main;

import Entreprise.Department;
import PrincipalApplication.EmployeeManager;
import Serialization.Serialization;
import Serveur.Server;
import PrincipalApplication.ClockingManager;
import PrincipalApplication.TimeClockManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * This class 'MainServer' allows to create and launch the server.
 */
public class MainServer
{
    // - - - MAIN - - -
    /**
     * The main who launches the server application.
     */
    public static void main(String[] args) throws Exception {
        final String DEPARTMENT_FILE = "departments.ser";

        //We create an object from the 'ClockingManager' class to be able to launch the server
        ClockingManager clockingManager = new ClockingManager();
        TimeClockManager timeClockManager = new TimeClockManager();
        ObservableList<Department> departments = FXCollections.observableArrayList();

        // Loading of previously saved departments
        List<Department> loaded = (List<Department>) Serialization.loadObject(DEPARTMENT_FILE);

        // if loaded exists, it's added to our ObservableList
        if (loaded != null) {
            departments.addAll(loaded);
        }

        // Manager of our employees which use our department list
        EmployeeManager employeeManager = new EmployeeManager(departments);
        //We instantiate the server
        Server myServer = new Server(clockingManager, timeClockManager, employeeManager);

        //we start the server
        myServer.start();

        System.out.println("The server is lunched");

        //allows you to not stop the program and to run it continuously
        Thread.currentThread().join();
    }
}