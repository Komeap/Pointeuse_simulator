package Planning;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * class representing an employee's weekly schedule.
 * It uses a HashMap to link a specific day of the week to its working hours
 */
public class Planning implements Serializable {

	private Map<DayOfWeek, WorkDay> scheduleMap; /** HashMap to store the working days */

	/**
	 * builds a planning object and initializes the map
	 */
	public Planning() {
		this.scheduleMap = new HashMap<>();
	}

	/**
	 * returns the workday schedule for a given day
	 * @param day : DayOfWeek
	 * @return workDay : WorkDay
	 */
	public WorkDay getWorkDay(DayOfWeek day) {
		return scheduleMap.get(day);
	}

	/**
	 * sets or updates a workday shift for a specific day
	 * @param day : DayOfWeek
	 * @param workDay : WorkDay
	 */
	public void setWorkDay(DayOfWeek day, WorkDay workDay) {
		scheduleMap.put(day, workDay);
	}
}