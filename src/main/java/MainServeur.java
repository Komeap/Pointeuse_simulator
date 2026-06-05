import Serveur.Server;
import PrincipalApplication.ClockingManager;

public class MainServeur {
    public static void main(String[] args) throws Exception {
        // 1. On crée le gestionnaire de données indispensables au serveur
        ClockingManager clockingManager = new ClockingManager();

        // 2. On instancie le serveur en lui transmettant le gestionnaire
        Server monServeur = new Server(clockingManager);

        // 3. On lance le serveur (il s'exécute alors en tâche de fond)
        monServeur.demarrer();

        System.out.println("Le serveur autonome est en cours d'exécution...");
//aj
        // Blocage pour éviter que le programme 'main' ne s'arrête immédiatement
        Thread.currentThread().join();
    }
}