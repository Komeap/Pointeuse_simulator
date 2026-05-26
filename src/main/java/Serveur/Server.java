package Serveur;

import Check.Check; // <-- INDISPENSABLE pour corriger l'erreur "cannot find symbol class Check"
import AppliCationPrincipale.GestionPointage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {

    private final GestionPointage gestionPointage;
    private static int port = 5001;
    private static ServerSocket serverSocket;

    public Server(GestionPointage gestionPointage)
    {
        this.gestionPointage = gestionPointage;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int val) {
        port = val;
    }

    public static void changerPort(int newPort) throws IOException {

        // Ferme l'ancien serveur si existant
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        setPort(newPort);

        // Recréation du serveur
        serverSocket = new ServerSocket(getPort());

        System.out.println("Server lancé sur le port " + getPort());
    }

    public void demarrer() {
        Thread serveurThread = new Thread(() -> {
            // F2 : Restauration de l'historique au démarrage
            @SuppressWarnings("unchecked")
            List<Check> charge = (List<Check>) Serialisation.loadObject("base_centrale.ser");
            if (charge != null) {
                gestionPointage.restaurerHistorique(charge);
                System.out.println("Historique central restauré : " + gestionPointage.getHistoriqueGlobal().size() + " pointages.");
            }

            try {
                // Initialisation du socket sur le port configuré
                changerPort(getPort());

                while (true) {
                    // On attend qu'une pointeuse se connecte
                    try (
                            Socket socket = serverSocket.accept();
                            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())
                    ) {
                        // Lecture du message réseau de la pointeuse
                        Message msg = (Message) ois.readObject();

                        // Conversion de Message en objet métier Check pour la table de l'IHM principale
                        Check nouveauCheck = gestionPointage.creerPointageAutomatique(
                                msg.getIdEmp(),
                                msg.getDate().toLocalDate(),
                                msg.getDate().toLocalTime()
                        );

                        // F1 : Enregistrement et mise à jour automatique de l'IHM
                        gestionPointage.ajouterPointage(nouveauCheck);

                        // F2 : Sauvegarde physique instantanée
                        Serialisation.saveObject(gestionPointage.getHistoriqueGlobal(), "base_centrale.ser");

                        System.out.println("Pointage reçu et sauvegardé pour l'employé : " + msg.getIdEmp());

                    } catch (IOException | ClassNotFoundException e) {
                        // Évite de faire crasher le serveur entier si une seule connexion réseau échoue
                        System.out.println("Erreur lors de la réception d'un pointage : " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("Erreur critique du serveur : " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Le thread s'arrêtera proprement dès que la fenêtre de l'IHM principale sera fermée
        serveurThread.setDaemon(true);
        serveurThread.start();
    }
}