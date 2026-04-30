import java.io.*;
import java.net.*;
import java.util.*;


public class Serveur {

    private static List<Message> historiqueGlobal = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        int port = 5000; // port random pour l'instant

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur en attente sur le port " + port);

        while(true){
            // Ca wait jusqu'a ce qu'il y ait une connexion
            try (Socket socket = serverSocket.accept();
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                Message msg = (Message) ois.readObject();

                // Enregistrement dans la structure de données (F1)
                historiqueGlobal.add(msg);

                // Sauvegarde immédiate ou périodique (F2)
                testSerialisation.saveObject(historiqueGlobal, "base_centrale.ser");

                System.out.println("Pointage reçu et sauvegardé pour l'employé : " + msg.getIdEmp());
            }
        }
    }
}