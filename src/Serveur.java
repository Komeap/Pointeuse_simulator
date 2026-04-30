import java.io.*;
import java.net.*;
<<<<<<< HEAD
import java.util.*;


public class Serveur {

    private static List<Message> historiqueGlobal = new ArrayList<>();
=======
import java.util.ArrayList;
import java.util.List;


public class Serveur {
    private static List<Message> archivePointages = new ArrayList<>();
>>>>>>> e863dbab7ee0cc4d9b20a10e70413ee13774f0d6

    public static void main(String[] args) throws Exception {
        int port = 5000; // port random pour l'instant

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur en attente sur le port " + port);

        while(true){
            // Ca wait jusqu'a ce qu'il y ait une connexion
            try (Socket socket = serverSocket.accept();
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                Message msg = (Message) ois.readObject();

<<<<<<< HEAD
                // Enregistrement dans la structure de données (F1)
                historiqueGlobal.add(msg);
=======
            archivePointages.add(msg); // AJout du pointage a la list

            System.out.println("Données reçues : emp" + msg.getIdEmp() + ", heure" + msg.getDate() + ", check" + msg.getType());
            System.out.println("Date : " + msg.getDate());
>>>>>>> e863dbab7ee0cc4d9b20a10e70413ee13774f0d6

                // Sauvegarde immédiate ou périodique (F2)
                testSerialisation.saveObject(historiqueGlobal, "base_centrale.ser");

                System.out.println("Pointage reçu et sauvegardé pour l'employé : " + msg.getIdEmp());
            }
        }
    }
}