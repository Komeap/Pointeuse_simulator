package Serveur;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Serveur {

    private static List<Message> historiqueGlobal = new ArrayList<>();
    private static int port = 5001;

    private static ServerSocket serverSocket;

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

        System.out.println("Serveur lancé sur le port " + getPort());
    }

    public static void main(String[] args) throws Exception {

        List<Message> charge =
                (List<Message>) Serialisation.loadObject("base_centrale.ser");

        if (charge != null) {
            historiqueGlobal.addAll(charge);
        }

        // lancement initial
        changerPort(getPort());

        while (true) {

            try (
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois =
                            new ObjectInputStream(socket.getInputStream())
            ) {

                Message msg = (Message) ois.readObject();

                historiqueGlobal.add(msg);

                Serialisation.saveObject(
                        historiqueGlobal,
                        "base_centrale.ser"
                );

                System.out.println(
                        "Pointage reçu et sauvegardé pour l'employé : "
                                + msg.getIdEmp()
                );
            }
        }
    }
}