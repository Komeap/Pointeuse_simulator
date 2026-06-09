package Serveur;

import Check.Check;
import PrincipalApplication.ClockingManager;
import PrincipalApplication.EmployeeManager;
import PrincipalApplication.PointeuseManager;

import java.io.ObjectInputStream;
import java.net.Socket;

class PointeuseHandler implements Runnable {

    private final Socket socket;
    private final ClockingManager clockingManager;
    private final PointeuseManager pointeuseManager;
    private final EmployeeManager employeeManager;
    private static final String EXPECTED_TOKEN = "Viv3P0LYTECH2026!!!";

    // setting up timeclock handler
    public PointeuseHandler(Socket socket, ClockingManager clockingManager, PointeuseManager pointeuseManager, EmployeeManager employeeManager) {
        this.socket = socket;
        this.clockingManager = clockingManager;
        this.pointeuseManager = pointeuseManager;
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
                System.out.println("connexion rejetée: signature invalide depuis " + s.getInetAddress());
                return;
            }

            boolean employeeExists = employeeManager.getEmployeeList().stream()
                    .anyMatch(e -> e.getEmployeeId().equals(messageReceived.getIdEmp()));

            if (!employeeExists) {
                System.out.println("Pointage rejeté : Employé inconnu (" + messageReceived.getIdEmp() + ")");
                return; // On arrête là, le pointage n'est jamais enregistré
            }

            // NOUVEAU : On enregistre la pointeuse si elle n'existe pas encore dans notre fichier
            pointeuseManager.registerPointeuseIfNotExists(messageReceived.getPointeuseId());

            // We calcul automatically if the check is IN or OUT
            Check newCheck = clockingManager.createAutomaticClocking(
                    messageReceived.getIdEmp(),
                    messageReceived.getDate().toLocalDate(),
                    messageReceived.getDate().toLocalTime()
            );

            // We add this new Check to our checks
            clockingManager.addClocking(newCheck);

            // we inform the user that we have received the check
            System.out.println("Pointage reçu de l'employé " + messageReceived.getIdEmp() + " via la pointeuse " + messageReceived.getPointeuseId());

        } catch (Exception error) {
            System.out.println("Erreur réception pointage : " + error.getMessage());
        }
    }
}