package calculations;

import com.example.FirstClass;
import kotlin.Pair;
import kotlin.ranges.ClosedRange;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@PlanningSolution
public class CourseSchedule {


    Logger logger = LoggerFactory.getLogger("CourseSchedule");
    private FirstClass TotalJson;
    private List<Lecture> lectureList;
    private List<Pair<String, Boolean>> roomList;
    private List<Pair<ClosedRange<LocalDateTime>, Boolean>> periodList;
    private List<Pair<String, Boolean>> teachersList;
    private List<Pair<ArrayList<Integer>, Boolean>> entriesList;
    private List<Pair<String, Boolean>> daysList;
    private HardSoftScore score;

    public FirstClass getTotalJson() {
        return TotalJson;

    }

    public void setTotalJson(FirstClass totalJson) {
        TotalJson = totalJson;
    }

    @ValueRangeProvider(id = "availableEntries")
    @ProblemFactCollectionProperty
    public List<Pair<ArrayList<Integer>, Boolean>> getEntriesList() {
        return entriesList;
    }

    public CourseSchedule() {
        roomList = new ArrayList<>();
        teachersList = new ArrayList<>();
        periodList = new ArrayList<>();
        lectureList = new ArrayList<>();
        entriesList = new ArrayList<>();
        daysList = new ArrayList<>();
    }

    @ValueRangeProvider(id = "availableDays")
    @ProblemFactCollectionProperty
    public List<Pair<String, Boolean>> getDaysList() {
        return daysList;
    }

    @ValueRangeProvider(id = "availableTeachers")
    @ProblemFactCollectionProperty
    public List<Pair<String, Boolean>> getTeachersList() {
        return teachersList;
    }

    @ValueRangeProvider(id = "availableRooms")
    @ProblemFactCollectionProperty
    public List<Pair<String, Boolean>> getRoomList() {
        return roomList;
    }

    @ValueRangeProvider(id = "availablePeriods")
    @ProblemFactCollectionProperty
    public List<Pair<ClosedRange<LocalDateTime>, Boolean>> getPeriodList() {
        return periodList;
    }

    @PlanningEntityCollectionProperty
    public List<Lecture> getLectureList() {
        return lectureList;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

/*
    public void printCourseSchedule() {
        lectureList.stream()
                .map(c -> "Lecture in Room " + c.getRoomNumber().toString() + " during Period " + c.getPeriod().toString() + "with teacher" + c.getTeacher().toString())
                .forEach(k -> logger.info(k));
    }
*/

}