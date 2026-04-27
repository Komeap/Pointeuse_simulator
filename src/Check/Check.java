package Check;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.io.Serializable;

public class Check implements Serializable{
	private LocalDate date;
	private LocalTime time;
	private CheckType type;
	private UUID employeeUUID;

	public Check(LocalDate nDate, LocalTime nTime, CheckType nType, UUID nEmployeeUUID) {
		date = nDate;
		time = nTime;
		type = nType;
		employeeUUID = nEmployeeUUID;
	}

	public LocalDate getDate() {return date;}
	public LocalTime getTime() {return time;}
	public CheckType getCheckType() {return type;}
	public UUID getEmployeeUUID() {return employeeUUID;}

	public void setDate(LocalDate newDate) {date = newDate;}
	public void setTime(LocalTime newTime) {time = newTime;}
	public void setCheckType(CheckType newCheckType) {type = newCheckType;}
	public void setEmployeeUUID(UUID newUUID) {employeeUUID = newUUID;}

	public static void main(String[] args) {};
}
