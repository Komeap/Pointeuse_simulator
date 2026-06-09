package Serveur;

import Check.Check;
import PrincipalApplication.ClockingManager;
import Serveur.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

class PointeuseHandler implements Runnable {

    private final Socket socket;
    private final ClockingManager clockingManager;
    private static final String EXPECTED_TOKEN = "Viv3P0LYTECH2026!!!";

    public PointeuseHandler(Socket socket, ClockingManager clockingManager) {
        this.socket = socket;
        this.clockingManager = clockingManager;
    }

    @Override
    public void run() {
        try (
                Socket s = this.socket;
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream()) //We recup the datas who are sent
        ) {
            //we read the message received, then we cast it into 'Check'
            Message messageReceived = (Message) ois.readObject();

            //verification of the safety token
            if (messageReceived.getSecurityToken() == null || !messageReceived.getSecurityToken().equals(EXPECTED_TOKEN)) {
                System.out.println("connexion rejetée: signature invalide depuis " + s.getInetAddress());
                return;
            }

            //We calcul automatically if the check is IN or OUT
            Check newCheck = clockingManager.createAutomaticClocking(
                    messageReceived.getIdEmp(),
                    messageReceived.getDate().toLocalDate(),
                    messageReceived.getDate().toLocalTime()
            );

            //We add this new Check to our checks
            clockingManager.addClocking(newCheck);

            //we inform the user that we have received the check
            System.out.println("Pointage reçu : " + messageReceived.getIdEmp());

        } catch (Exception error) {
            System.out.println("Erreur réception pointage : " + error.getMessage());
        }
    }
}