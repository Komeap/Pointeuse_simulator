package Planning;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing an employee's weekly schedule.
 * It uses a HashMap to link a specific day of the week to its working hours.
 */
public class Planning implements Serializable {

	// HashMap to store the working days
	private Map<DayOfWeek, WorkDay> scheduleMap;

	// Constructor: initializes an empty schedule
	public Planning() {
		this.scheduleMap = new HashMap<>();
	}

	// Adds or updates the working hours for a specific day
	public void setWorkDay(DayOfWeek day, WorkDay workDay) {
		scheduleMap.put(day, workDay);
	}

	// Gets the working hours for a given day (returns null if the employee is not working)
	public WorkDay getWorkDay(DayOfWeek day) {
		return scheduleMap.get(day);
	}
}