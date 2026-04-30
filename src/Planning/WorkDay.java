package Planning;

import java.io.Serializable;
import java.time.LocalTime;

public class WorkDay implements Serializable {

    private LocalTime startTime;
    private LocalTime endTime;

    public WorkDay(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return startTime + " -> " + endTime;
    }
}