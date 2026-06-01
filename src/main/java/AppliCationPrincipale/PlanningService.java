package AppliCationPrincipale;

import Employee.Employee;
import Planning.WorkDay;
import Serveur.Serialisation;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

import java.time.DayOfWeek;
import java.util.List;

public class PlanningService {

    public void chargerPlanning(HBox barre, Label labelInfo, String id) {
        // Récupération de l'employé via la base de données
        List<Employee> employees = (List<Employee>) Serialisation.loadObject("employees.ser");
        assert employees != null;
        Employee emp = employees.stream()
                .filter(e -> e.getEmployeeId().toString().equals(id))
                .findFirst()
                .orElse(null);

        if (emp != null && emp.getPlanning() != null) {
            barre.getChildren().clear();
            barre.setSpacing(1);

            // On récupère le premier jour du planning
            WorkDay j = emp.getPlanning().getWorkDay(DayOfWeek.valueOf("monday"));

            // Calcul des index (1 heure = 4 quarts d'heure)
            int debut = j.getStartTime().getHour() * 4 + (j.getStartTime().getMinute() / 15);
            int fin = j.getEndTime().getHour() * 4 + (j.getEndTime().getMinute() / 15);

            for (int i = 0; i < 96; i++) {
                Rectangle r = new Rectangle(10, 20);
                r.setFill((i >= debut && i < fin) ? Color.CORNFLOWERBLUE : Color.LIGHTGRAY);
                barre.getChildren().add(r);
            }

            labelInfo.setText("Horaire : " + j.getStartTime() + " - " + j.getEndTime());
        } else {
            labelInfo.setText("Aucun planning trouvé pour cet employé.");
        }
    }
}