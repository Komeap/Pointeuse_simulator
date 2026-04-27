import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import Check.CheckType;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID idEmp;
    private CheckType type;
    private LocalDateTime date;

    public Message(UUID texte, CheckType valeur, LocalDateTime date) {
        this.idEmp = texte;
        this.type = valeur;
        this.date = date;
    }

    // Getters
    public UUID getIdEmp() { return idEmp; }
    public CheckType getType() { return type; }
    public LocalDateTime getDate() { return date; }
}