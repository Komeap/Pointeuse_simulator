import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Serveur {
    private static List<Message> archivePointages = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        int port = 5000; // port random pour l'instant

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur en attente sur le port " + port);

        while(true){
            // Ca wait jusqu'a ce qu'il y ait une connexion
            Socket socket = serverSocket.accept();

            // Flux pour lire l'objet
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message msg = (Message) ois.readObject();

            archivePointages.add(msg); // AJout du pointage a la list

            System.out.println("Données reçues : emp" + msg.getIdEmp() + ", heure" + msg.getDate() + ", check" + msg.getType());
            System.out.println("Date : " + msg.getDate());

            ois.close();
            socket.close();
        }
    }
}