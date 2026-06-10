PROJET TUTORE JAVA
membre du groupe : ESPITALIER Tiago - PINAUD Evan - COINTRE Pierre - HENDAOUI Sofian - TOULZAC Julien

1. Organisation de nos sources et de nos classes :
Pour ce projet, nous avons fait le choix de regrouper l'ensemble de nos sources au sein d'un unique projet IntelliJ afin d’avoir toutes les données. 
Nous avons défini plusieurs méthodes main distinctes pour permettre l'exécution séparée des différents programmes attendus. Enfin, pour faciliter notre travail, nous nous 
sommes appuyés sur Gradle et les configurations d'exécution d'IntelliJ pour paramétrer un lancement global de l'application.

2. Ordre de lancement de nos programmes : 
EN haut de l'application IntelliJ, à l'endroit pour lancer le programme, vous avez plusieurs choix possibles : 
- 'LANCEMENT TOTAL' : ceci permet de lancer nos deux applications simultanément, nous recommandons ce système de lancement.
- vous pouvez lancer individuellement nos deux applications avec 'principale' et 'pointeuse'. Il n'y a pas d'ordre de lancement entre les deux.
- vous pouvez également lancer 'serveur', il faudra changer les ports du serveur dans serveur

3. Contribution des membres du groupe :
- HENDAOUI Sofian : Conception des plannings hebdomadaire (Planning, WorkDay) avec affichage graphique sous forme de frise dynamique de 96 quarts d’heure colorés via des Listeners JavaFX.
Implémentation du module de filtrage croisé de l'interface et création complète du sous-système de gestion des plannings, générer pour 35h, grilles par paliers de 15 min et 
gestion du piège de minuit. Intégration de verrous d'intégrité algorithmiques calculant le temps de travail en temps réel et bloquant les saisies d'horaires incohérents ou inversés.
- COINTRE Pierre : Développement complet du squelette et du design CSS de l'IHM principale, incluant la coloration des alertes horaires (rouge/orange) et la synchronisation des données 
d'affichage, création et gestion des composants métiers (DepartmentManager, gestion IHM des employés, verrous de suppression en cascade) avec sauvegarde par sérialisation en temps réel.
Implémentation du système complet de configuration dynamique (IP, port, cycle réseau via TimeClockConfig) persistant sur fichier pour synchroniser le serveur et la pointeuse.
- PINAUD Evan : Développement des structures Department et Serialization générique et gestion dynamique des fichiers de données (employees.ser et buffer_pointeuse.ser).Implémentation 
des boutons de manipulation de l'IHM principale avec EmployeeManager, ClockingManager, incluant les fenêtres de dialogue d'alerte et la synchronisation en directe des tableaux.
Finalisation du Server et de la logique réseau de TimeClockMMI pour assurer la liaison et l'affichage des pointages en temps réel lors d'une validation.*
- ESPITALIE Tiago : 
Server en architecture multi-threadée (TimeClockHandler) pour gérer la connexion sécurisée (par token) de pointeuses multiples. Évolutions IHM avec PrincipalApplicationMMI et 
EmployeeManager, avec mise en place d'un système de filtrage croisé performant et validation de l'intégrité des données. Conception du routeur de configurations dynamiques partagées 
pour les terminaux et intégration d'un module d'import de fichiers de pointage au format CSV.
- TOULZAC Julien : Conception de l'architecture réseau (Serveur, Message) et sécurisation du flux de données par sérialisation avec un buffer d'historique. Gestion du cycle de vie 
du projet (build Gradle pour les dépendances JavaFX, gestion des conflits Git et simplification de la compilation multiplateforme).Egalement le développement complet de 
l'IHM TimeClockMMI (Pointeuse JavaFX, horloge dynamique et thread d'envoi) et interconnexion fonctionnelle des employés et départements dans l'IHM principale.

