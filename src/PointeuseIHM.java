import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PointeuseIHM {
    public static void main(String[] args){
        JFrame pointeuse = new JFrame("Pointeuse Emulateur");
        pointeuse.setSize(400, 300);
        pointeuse.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pointeuse.setLayout(new BorderLayout());

        // affichage de la date et heure
        JLabel labelDate = new JLabel("Chargement...", SwingConstants.CENTER);
        labelDate.setFont(new Font("Arial", Font.PLAIN, 20));

        JLabel labelHeure = new JLabel("Chargement...", SwingConstants.CENTER);
        labelHeure.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel labelRoundHeure = new JLabel("", SwingConstants.CENTER);
        labelRoundHeure.setFont(new Font("Arial", Font.PLAIN, 12));

        String[] options = {"Pierre Cointre", "Tiago Espitalier", "Julien Toulzac"};
        JComboBox<String> choixEmployer = new JComboBox<>(options);

        JButton check = new JButton("Check in/out");

        JPanel panneauTemps = new JPanel(new GridLayout(3, 1));
        panneauTemps.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panneauTemps.add(labelDate);
        panneauTemps.add(labelHeure);
        panneauTemps.add(labelRoundHeure);

        JPanel panneauControles = new JPanel();
        panneauControles.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panneauControles.add(choixEmployer);
        panneauControles.add(check);

        pointeuse.add(panneauTemps, BorderLayout.CENTER);
        pointeuse.add(panneauControles, BorderLayout.SOUTH);

        check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Bouton check cliqué, Il faut envoyer les donnés de pointages !!!!!!");
            }
        });

        choixEmployer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // On récup l'elt sélec
                String selection = (String) choixEmployer.getSelectedItem();
                System.out.println("Vous avez sélectionné : " + selection);
            }
        });


        Timer timer = new Timer(1000, e -> {
            // Gestion de l'heure actuelle et formatage
            LocalDateTime monHeure = LocalDateTime.now(); // prend l'heure actuelle
            DateTimeFormatter formateurH = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH); // création du mask format
            String localHourString = monHeure.format(formateurH); // mise au format
            labelHeure.setText(localHourString); // met a jour la variable du label

            // Gestion de la date actuelle et formatage
            LocalDateTime maDate = LocalDateTime.now();
            DateTimeFormatter formateur = DateTimeFormatter.ofPattern("EEEE d MMMM, yyyy", Locale.FRENCH);
            String localDateString = maDate.format(formateur);
            labelDate.setText(localDateString);

            // Gestion de l'heure arrondie au qart d'heure le plus proche actiuelle et formatage
            int modulo = monHeure.getMinute()%15; // recup la dif d'heure entre actuelle et le quart d'heure le plus proche
            int minutesToAdd = (modulo < 8) ? -modulo : (15 - modulo); // met une variavble a lheure qu'il faut ajouter celon le quart d'heure
            LocalDateTime roundedHour =  monHeure.plusMinutes(minutesToAdd).truncatedTo(ChronoUnit.MINUTES); // arrondisement avec les minutes a ajouter pour arriver au quart d'heure
            DateTimeFormatter formateurHR = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.FRENCH); // format
            String roundedHourString = "Contabilisé a l'heure : " + roundedHour.format(formateurHR); // ajout du texte
            labelRoundHeure.setText(roundedHourString); // mise  a jour de la variable


        });
        timer.start();

        pointeuse.setVisible(true);
    }
}