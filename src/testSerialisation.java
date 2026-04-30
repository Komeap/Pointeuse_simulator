import java.util.UUID;
import java.io.*;

public class testSerialisation implements Serializable {
    private final UUID id;
    private String name;
    private int age;

    testSerialisation(String sName, int iAge)
    {
        id = UUID.randomUUID();
        name = sName;
        age = iAge;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String sName) {
        this.name = sName;
    }
    public String getName() {
        return name;
    }

    public void setAge(int iAge) { this.age = iAge; }
    public int getAge() {
        return age;
    }

    public static void saveObject(Object obj, String fileName)
    {
        try(FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oss = new ObjectOutputStream(fos))
        {
            oss.writeObject(obj);
            System.out.println("Objet sauvegardé");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object loadObject(String fileName) {
        try (FileInputStream fos = new FileInputStream(fileName);
             ObjectInputStream oos = new ObjectInputStream(fos))
        {
            return oos.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        testSerialisation monEmploye = new testSerialisation("Kevin", 25);

        saveObject(monEmploye, "test.ser");

        testSerialisation objetRecupere = (testSerialisation) loadObject("test.ser");

        if (objetRecupere != null) {
            System.out.println("ID récupéré : " + objetRecupere.getId());
            System.out.println("Nom récupéré : " + objetRecupere.getName());
        }
    }

}
