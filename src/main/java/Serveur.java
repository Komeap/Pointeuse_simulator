import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Serveur {
    private static List<Message> historiqueGlobal = new ArrayList<>();
    private static int port = 5001;

    public static int getPort(){
        return port;
    }
    public static void setPort(int val){
        port = val;
    }
    public static void changerPort(int newPort) throws IOException {
        setPort(newPort);
        ServerSocket serverSocket = new ServerSocket(getPort());
        System.out.println("Serveur en attente sur le port " + getPort());
    }
    public static void main(String[] args) throws Exception {

        List<Message> charge = (List<Message>) Serialisation.loadObject("base_centrale.ser");
        if (charge != null)
            historiqueGlobal.addAll(charge);

        ServerSocket serverSocket = null;
        Serveur.changerPort(getPort());

        while(true) {
            // Ca wait jusqu'a ce qu'il y ait une connexion
            try (Socket socket = serverSocket.accept();
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                Message msg = (Message) ois.readObject();

                // Enregistrement dans la structure de données (F1)
                historiqueGlobal.add(msg);

                // Sauvegarde immédiate ou périodique (F2)
                Serialisation.saveObject(historiqueGlobal, "base_centrale.ser");

                System.out.println("Pointage reçu et sauvegardé pour l'employé : " + msg.getIdEmp());
            }
        }
    }
}