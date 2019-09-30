package calculations;

import kotlin.Pair;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lecture {

    private Pair<String, Boolean> roomNumber;
    private Pair<String, Boolean> period;
    private Pair<String, Boolean> teacher;
    private Pair<String, Boolean> entry;
    private Pair<String, Boolean> day;


    @PlanningVariable(valueRangeProviderRefs = {"availableDays"})
    public Pair<String, Boolean> getDay() {
        return day;
    }

    public void setDay(Pair<String, Boolean> day) {
        this.day = day;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availableEntries"})
    public Pair<String, Boolean> getEntry() {
        return entry;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availableTeachers"})
    public Pair<String, Boolean> getTeacher() {
        return teacher;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availablePeriods"})
    public Pair<String, Boolean> getPeriod() {
        return period;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availableRooms"})
    public Pair<String, Boolean> getRoomNumber() {
        return roomNumber;
    }

    public void setEntry(Pair<String, Boolean> entry) {
        this.entry = entry;
    }

    public void setTeacher(Pair<String, Boolean> teacher) {
        this.teacher = teacher;
    }

    public void setPeriod(Pair<String, Boolean> period) {
        this.period = period;
    }

    public void setRoomNumber(Pair<String, Boolean> roomNumber) {
        this.roomNumber = roomNumber;
    }
}