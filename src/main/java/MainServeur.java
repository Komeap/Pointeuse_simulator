import Serveur.Serveur;
import AppliCationPrincipale.GestionPointage;

public class MainServeur {
    public static void main(String[] args) throws Exception {
        // 1. On crée le gestionnaire de données indispensables au serveur
        GestionPointage gestionPointage = new GestionPointage();

        // 2. On instancie le serveur en lui transmettant le gestionnaire
        Serveur monServeur = new Serveur(gestionPointage);

        // 3. On lance le serveur (il s'exécute alors en tâche de fond)
        monServeur.demarrer();

        System.out.println("Le serveur autonome est en cours d'exécution...");

        // Blocage pour éviter que le programme 'main' ne s'arrête immédiatement
        Thread.currentThread().join();
    }
}