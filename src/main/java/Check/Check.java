package Check;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Objects;

public class Check implements Serializable{
	private static final long serialVersionUID = 1L;

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

	@Override
	public int hashCode() {
		return Objects.hash(date, time, type, employeeUUID);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Check check = (Check) o;
		return Objects.equals(date, check.date) &&
				Objects.equals(time, check.time) &&
				type == check.type &&
				Objects.equals(employeeUUID, check.employeeUUID);
	}

	public static void main(String[] args) {};
}
