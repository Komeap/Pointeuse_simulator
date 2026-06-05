/**
 * The 'Check' class represents a single check in the system.
 * It stocks information related to this check.
 */

package Check;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Objects;

public class Check implements Serializable
{
	//private static final long serialVersionUID = 1L;

	//- - - ATTRIBUTES - - -
	private LocalDate date; //date of clocking
	private LocalTime time; //hour of clocking
	private CheckType type; //type of clocking (IN or OUT)
	private UUID employeeUUID; //id of employee who checked

	// - - - CONSTRUCTOR
	public Check(LocalDate nDate, LocalTime nTime, CheckType nType, UUID nEmployeeUUID)
	{
		date = nDate;
		time = nTime;
		type = nType;
		employeeUUID = nEmployeeUUID;
	}

	// - - - GETTERS - - -
	public LocalDate getDate() {return date;}
	public LocalTime getTime() {return time;}
	public CheckType getCheckType() {return type;}
	public UUID getEmployeeUUID() {return employeeUUID;}

	// - - - SETTERS - - -
	public void setDate(LocalDate newDate) {date = newDate;}
	public void setTime(LocalTime newTime) {time = newTime;}
	public void setCheckType(CheckType newCheckType) {type = newCheckType;}
	public void setEmployeeUUID(UUID newUUID) {employeeUUID = newUUID;}

	//- - - METHODS - - -
	/**
	 * return a unique int for the object
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(date, time, type, employeeUUID);
	}

	/**
	 * Redefine the method equals.
	 */
	@Override
	public boolean equals(Object o)
	{
		//If it’s exactly the same instance => they are equal
		if (this == o)
			return true;
		//If the other object is null or of another class => not equal
		if (o == null || getClass() != o.getClass())
			return false;

		//we convert (cast) the object in parameter in Check
		Check check = (Check) o;
		//We check that the all attributes are the same
		return Objects.equals(date, check.date) && Objects.equals(time, check.time) && type == check.type && Objects.equals(employeeUUID, check.employeeUUID);
	}

	public static void main(String[] args) {};
}
