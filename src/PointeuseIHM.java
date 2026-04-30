import Check.CheckType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import java.net.Socket;
import java.io.ObjectOutputStream;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;


// Faudra import message quand il sera plus dans le dossier principale si on le bouge

public class PointeuseIHM {

    private static List<Message> bufferPointages = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args){

        demarrerThreadEnvoi(); // Debut du thread

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
                UUID testid = UUID.randomUUID();
                CheckType testcheck = CheckType.OUT;
                Message msg = new Message(testid, testcheck, LocalDateTime.now());

                bufferPointages.add(msg);
                System.out.println("Pointage mis en attente. Total dans le buffer : " + bufferPointages.size());
                /*try {
                    Socket socket = new Socket("localhost", 5000);

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    oos.writeObject(msg);
                    oos.flush();

                    System.out.println("Succès : Les données de pointage ont été envoyées !");

                    oos.close();
                    socket.close();

                } catch (Exception ex) {
                    System.out.println("Erreur de connexion : Le serveur est-il bien lancé ?");
                    ex.printStackTrace();
                }*/
            }
        });

        choixEmployer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // On récup l'elt sélec
                String employe = (String) choixEmployer.getSelectedItem();
                System.out.println("Vous avez sélectionné : " + employe);
            }
        });

        //Action a faire chaque seconde
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

    private static void demarrerThreadEnvoi() {
        new Thread(() -> {

            while (true) {
                try {
                    // On endort le Thread pendant 5 secondes (5000 millisecondes)
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // SI buffer pas vide, on le vide
                if (!bufferPointages.isEmpty()) {
                    System.out.println("🔄 Tentative d'envoi... (" + bufferPointages.size() + " message(s) en attente)");

                    while (!bufferPointages.isEmpty()) {
                        Message messageAEnvoyer = bufferPointages.get(0);
                        // ON essaie de vidé si on peut
                        try {
                            Socket socket = new Socket("localhost", 5000);
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                            oos.writeObject(messageAEnvoyer);
                            oos.flush();

                            oos.close();
                            socket.close();

                            // Succès : on retire le message
                            bufferPointages.remove(0);
                            System.out.println("✅ Message envoyé au serveur !");

                        } catch (Exception ex) {
                            System.out.println("Serveur injoignable, Fin de la tentative, on réessayera au prochain cycle.");
                            break;
                        }
                    }
                }
            }

        }).start(); // On démarre le Thread
    }



    public static List<Message> getBufferPointages() {
        return bufferPointages;
    }

    public static void setBufferPointages(List<Message> bufferPointages) {
        PointeuseIHM.bufferPointages = bufferPointages;
    }
}