import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Planning implements Serializable {

	private Map<DayOfWeek, WorkDay> scheduleMap;

	public Planning() {
		this.scheduleMap = new HashMap<>();
	}

	public void setWorkDay(DayOfWeek day, WorkDay workDay) {
		scheduleMap.put(day, workDay);
	}

	public WorkDay getWorkDay(DayOfWeek day) {
		return scheduleMap.get(day);
	}
}