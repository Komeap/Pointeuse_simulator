package Planning;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Class representing a single working day for an employee.
 * It stores the start time and the end time of their shift.
 */
public class WorkDay implements Serializable {

    private LocalTime startTime; /** Start time of the shift */
    private LocalTime endTime; /** End time of the shift */

    /**
     * builds a workday object with start and end times
     * @param startTime : LocalTime
     * @param endTime : LocalTime
     */
    public WorkDay(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * returns the start time of the shift
     * @return startTime : LocalTime
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * returns the end time of the shift
     * @return endTime : LocalTime
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * returns a String  used for a correct display of the working hours.
     * @return formatted representation of the shift hours : String
     */
    @Override
    public String toString() {
        return startTime + " -> " + endTime;
    }
}