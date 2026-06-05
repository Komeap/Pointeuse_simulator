/**
 * This Message class allows you to stock the information received from a clocking
 * by the time clock on the server.
 */

package Serveur;

import Check.CheckType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Message implements Serializable
{
    //private static final long serialVersionUID = 1L;

    //- - - ATTRIBUTES - - -
    //attributes that we will receive from the time clock
    private UUID idEmp; //id of employee who has clocked
    private CheckType type; //type of check (IN or OUT)
    private LocalDateTime date; //date and hours of clocking

    //- - - CONSTRUCTOR - - -
    public Message(UUID texte, CheckType valeur, LocalDateTime date)
    {
        this.idEmp = texte;
        this.type = valeur;
        this.date = date;
    }

    // - - - GETTERS - - -
    public UUID getIdEmp() { return idEmp; }
    public CheckType getType() { return type; }
    public LocalDateTime getDate() { return date; }
}