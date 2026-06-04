package Serveur;

import java.io.*;

public class Serialisation implements Serializable {


    public static void saveObject(Object obj, String fileName)
    {
        try(FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oss = new ObjectOutputStream(fos))
        {
            oss.writeObject(obj);
            System.out.println("Objet sauvegardé");
        }
        catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static Object loadObject(String fileName) {
        try (FileInputStream fos = new FileInputStream(fileName);
             ObjectInputStream oos = new ObjectInputStream(fos))
        {
            return oos.readObject();
        }
        catch (IOException | ClassNotFoundException error) {
            error.printStackTrace();
            return null;
        }
    }

}
