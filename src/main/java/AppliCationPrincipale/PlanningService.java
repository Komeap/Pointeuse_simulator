package AppliCationPrincipale;

import Employee.Employee;
import Planning.WorkDay;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

import java.time.DayOfWeek;

public class PlanningService {

    /**
     * Charge  le planning visuel d'un employé pour un jour donné
     */
    public void chargerPlanning(HBox barre, Label labelInfo, Employee emp, DayOfWeek jour) {
        if (emp != null && emp.getPlanning() != null) {
            barre.getChildren().clear();
            barre.setSpacing(2);

            WorkDay j = emp.getPlanning().getWorkDay(jour);

            if (j != null && j.getStartTime() != null && j.getEndTime() != null) {
                // Calcul des index (1 heure = 4 quarts d'heure, 24h = 96 rectangles)
                int debut = j.getStartTime().getHour() * 4 + (j.getStartTime().getMinute() / 15);
                int fin = j.getEndTime().getHour() * 4 + (j.getEndTime().getMinute() / 15);

                for (int i = 0; i < 96; i++) {
                    Rectangle r = new Rectangle(12, 25);
                    // Si l'index est entre le début et la fin du travail Bleu, sinon Gris
                    r.setFill((i >= debut && i < fin) ? Color.CORNFLOWERBLUE : Color.LIGHTGRAY);
                    barre.getChildren().add(r);
                }
                labelInfo.setText("Planning du " + jour + " pour " + emp.getFirstName() + " : " + j.getStartTime() + " - " + j.getEndTime());
            } else {
                barre.getChildren().clear();
                labelInfo.setText("Aucune heure de travail définie ce jour pour " + emp.getFirstName());
            }
        } else {
            barre.getChildren().clear();
            labelInfo.setText("Aucun planning disponible pour cet employé.");
        }
    }
}