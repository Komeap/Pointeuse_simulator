/**
 * This class is used to launch the server on listen to receive the clocking
 * from the time clock and save them so that they can be displayed later
 * with the help of other classes.
 */

package Serveur;

import Check.Check;
import PrincipalApplication.ClockingManager;
import PrincipalApplication.EmployeeManager;
import PrincipalApplication.PointeuseManager;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //- - - ATTRIBUTES - - -
    //variable who allows to manage the clocking
    //'final' for that no one modify it
    private final ClockingManager clockingManager;
    private final PointeuseManager pointeuseManager;
    private  final EmployeeManager employeeManager;
    //variable server port
    //'static' because the server port it's unique and 'final' for that no one modify it
    private static final int PORT = 5005;

    //variable that listens and looks at incoming connections on the port (5005 here)
    //'static' so that there is only this class that instantiates it
    private static ServerSocket serverSocket;

    //thread pool to manage multiple connections
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    //- - - CONSTRUCTOR - - -

    /**
     * builds a server object
     * @param clockingManager : ClockingManager
     * @param pointeuseManager : PointeuseManager
     */
    public Server(ClockingManager clockingManager, PointeuseManager pointeuseManager, EmployeeManager employeeManager) {
        this.clockingManager = clockingManager;
        this.pointeuseManager = pointeuseManager;
        this.employeeManager = employeeManager;
    }

    //- - - METHOD - - -
    /**
     * this method lunch the server on the port so that he listens and add the new clocking it receives
     * The exceptions are managed.
     */
    public void start() {

        Thread serveurThread = new Thread(() -> { //We create a thread to avoid stop the program.


            try {
                serverSocket = new ServerSocket(PORT); //Initialisation socket server port
                System.out.println("Serveur lancé sur le port " + PORT); //We say to the user that the socket server is lunched

                //we keep the server running in a loop as long as the program is running so that we can listen continuously
                while (true) {
                    //we wait that a time clock connectes to the server
                    Socket socket = serverSocket.accept();

                    //delegation to a thread handler
                    threadPool.submit(new PointeuseHandler(socket, clockingManager, pointeuseManager, employeeManager));
                }

            } catch (Exception error) { //here, we manage the potential errors
                System.out.println("server error : " + error.getMessage());
                error.printStackTrace();
            }
        });

        serveurThread.setDaemon(true); //the server is stoped when the program is stoped
        serveurThread.start(); //We lunch the server
    }
}
