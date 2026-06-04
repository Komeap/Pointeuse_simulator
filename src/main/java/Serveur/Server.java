package Serveur;

import Check.Check;
import AppliCationPrincipale.GestionPointage;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {

    private final GestionPointage gestionPointage;
    private static final int PORT = 5005;
    private static ServerSocket serverSocket;

    public Server(GestionPointage gestionPointage) {
        this.gestionPointage = gestionPointage;
    }

    public void demarrer() {

        Thread serveurThread = new Thread(() -> {


            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("Serveur lancé sur le port " + PORT);

                while (true) {

                    try (
                            Socket socket = serverSocket.accept();
                            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
                    ) {

                        Message msg = (Message) ois.readObject();

                        Check nouveauCheck = gestionPointage.createAutomaticClocking(
                                msg.getIdEmp(),
                                msg.getDate().toLocalDate(),
                                msg.getDate().toLocalTime()
                        );

                        gestionPointage.addClocking(nouveauCheck);

                        System.out.println("Pointage reçu : " + msg.getIdEmp());

                    } catch (Exception e) {
                        System.out.println("Erreur réception pointage : " + e.getMessage());
                    }
                }

            } catch (Exception error) {
                System.out.println("server error : " + error.getMessage());
                error.printStackTrace();
            }
        });

        serveurThread.setDaemon(true);
        serveurThread.start();
    }
}