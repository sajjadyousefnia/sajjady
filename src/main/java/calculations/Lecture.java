package calculations;

import kotlin.Pair;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lecture {

    private Pair<Integer, Boolean> roomNumber;
    private Pair<Integer, Boolean> period;
    private Pair<Integer, Boolean> teacher;
    private Pair<Integer, Boolean> entry;

    @PlanningVariable(valueRangeProviderRefs = {"availableEntries"})
    public Pair<Integer, Boolean> getEntry() {
        return entry;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availableTeachers"})
    public Pair<Integer, Boolean> getTeacher() {
        return teacher;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availablePeriods"})
    public Pair<Integer, Boolean> getPeriod() {
        return period;
    }

    @PlanningVariable(valueRangeProviderRefs = {"availableRooms"})
    public Pair<Integer, Boolean> getRoomNumber() {
        return roomNumber;
    }

    public void setEntry(Pair<Integer, Boolean> entry) {
        this.entry = entry;
    }

    public void setTeacher(Pair<Integer, Boolean> teacher) {
        this.teacher = teacher;
    }

    public void setPeriod(Pair<Integer, Boolean> period) {
        this.period = period;
    }

    public void setRoomNumber(Pair<Integer, Boolean> roomNumber) {
        this.roomNumber = roomNumber;
    }
}