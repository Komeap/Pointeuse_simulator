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
    /**
     * attributes that we will receive from the time clock
     */
    private UUID idEmp; /** id of employee who has clocked */
private CheckType type; /** type of check (IN or OUT) */
private LocalDateTime date; /** date and hours of clocking */

private String securityToken; /**security token for a secured TCP */
private UUID pointeuseId; /**UUID for multi timeclock management */

    //- - - CONSTRUCTOR - - -
    /**
     * builds a Message Object
     * @param id : UUID
     * @param checkType : CheckType
     * @param date : LocalDateTime
     */
    public Message(UUID id, CheckType checkType, LocalDateTime date, String sToken, UUID pointeuseId)
    {
        this.idEmp = id;
        this.type = checkType;
        this.date = date;
        this.securityToken = sToken;
        this.pointeuseId = pointeuseId;
    }

    // - - - GETTERS - - -
    /**
     * returns the pointeuseId attribute
     * @return pointeuseId
     */
    public UUID getPointeuseId() { return pointeuseId; }

    /**
     * returns the idEmp attribute
     * @return idEmp
     */
    public UUID getIdEmp() { return idEmp; }
    /**
     * returns the type attribute
     * @return type
     */
    public CheckType getType() { return type; }
    /**
     * returns the date attribute
     * @return date
     */
    public LocalDateTime getDate() { return date; }

    public String getSecurityToken() { return securityToken;}
}