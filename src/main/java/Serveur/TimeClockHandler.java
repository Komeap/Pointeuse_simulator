package Serveur;

import Check.Check;
import PrincipalApplication.ClockingManager;
import PrincipalApplication.EmployeeManager;
import PrincipalApplication.TimeClockManager;

import java.io.ObjectInputStream;
import java.net.Socket;

class TimeClockHandler implements Runnable {

    private final Socket socket;
    private final ClockingManager clockingManager;
    private final TimeClockManager timeClockManager;
    private final EmployeeManager employeeManager;
    private static final String EXPECTED_TOKEN = "Viv3P0LYTECH2026!!!";

    // setting up timeclock handler
    public TimeClockHandler(Socket socket, ClockingManager clockingManager, TimeClockManager timeClockManager, EmployeeManager employeeManager) {
        this.socket = socket;
        this.clockingManager = clockingManager;
        this.timeClockManager = timeClockManager;
        this.employeeManager = employeeManager;
    }

    @Override
    public void run() {
        try (
                Socket s = this.socket;
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream()) // We recup the datas who are sent
        ) {
            // we read the message received, then we cast it into 'Message'
            Message messageReceived = (Message) ois.readObject();

            // verification of the safety token
            if (messageReceived.getSecurityToken() == null || !messageReceived.getSecurityToken().equals(EXPECTED_TOKEN)) {
                System.out.println("connexion rejected: invalid signature from " + s.getInetAddress());
                return;
            }

            boolean employeeExists = employeeManager.getEmployeeList().stream()
                    .anyMatch(e -> e.getEmployeeId().equals(messageReceived.getIdEmp()));

            if (!employeeExists) {
                System.out.println("Check rejected : Unknown employee (" + messageReceived.getIdEmp() + ")");
                return; // we stop there, the check is never registered
            }

            timeClockManager.registerPointeuseIfNotExists(messageReceived.getPointeuseId());

            // We calcul automatically if the check is IN or OUT
            Check newCheck = clockingManager.createAutomaticClocking(
                    messageReceived.getIdEmp(),
                    messageReceived.getDate().toLocalDate(),
                    messageReceived.getDate().toLocalTime()
            );

            // We add this new Check to our checks
            clockingManager.addClocking(newCheck);

            // we inform the user that we have received the check
            System.out.println("Check received of employee " + messageReceived.getIdEmp() + " from the timeclock " + messageReceived.getPointeuseId());

        } catch (Exception error) {
            System.out.println("Error reception check : " + error.getMessage());
        }
    }
}