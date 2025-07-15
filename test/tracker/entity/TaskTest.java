package tracker.entity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private final LocalDateTime testTime = LocalDateTime.now();
    private final Duration testDuration = Duration.ofMinutes(30);

    @Test
    void equalsByIdOnly() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW, testDuration, testTime);
        Task task2 = new Task(1, "Task 2", "Another desc", Status.DONE, Duration.ofHours(1), testTime.plusDays(1));
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны, независимо от других полей");
    }

    @Test
    void getEndTimeCalculation() {
        Task task = new Task(1, "Task", "Desc", Status.NEW, testDuration, testTime);
        assertEquals(testTime.plus(testDuration), task.getEndTime(),
                "Время окончания должно рассчитываться как startTime + duration");
    }

    @Test
    void getEndTimeWhenNoStartTime() {
        Task task = new Task(1, "Task", "Desc", Status.NEW, testDuration, null);
        assertNull(task.getEndTime(), "getEndTime() должен возвращать null при отсутствии startTime");
    }

    @Test
    void getEndTimeWhenNoDuration() {
        Task task = new Task(1, "Task", "Desc", Status.NEW, null, testTime);
        assertNull(task.getEndTime(), "getEndTime() должен возвращать null при отсутствии duration");
    }

    @Test
    void taskFieldsProperlySet() {
        Task task = new Task(1, "Task", "Desc", Status.IN_PROGRESS, testDuration, testTime);

        assertAll(
                () -> assertEquals(1, task.getId()),
                () -> assertEquals("Task", task.getName()),
                () -> assertEquals("Desc", task.getDescription()),
                () -> assertEquals(Status.IN_PROGRESS, task.getStatus()),
                () -> assertEquals(testDuration, task.getDuration()),
                () -> assertEquals(testTime, task.getStartTime())
        );
    }

    @Test
    void settersWorkCorrectly() {
        Task task = new Task(1, "Task", "Desc", Status.NEW, null, null);

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        Duration newDuration = Duration.ofHours(2);

        task.setName("New Name");
        task.setDescription("New Desc");
        task.setStatus(Status.DONE);
        task.setDuration(newDuration);
        task.setStartTime(newTime);

        assertAll(
                () -> assertEquals("New Name", task.getName()),
                () -> assertEquals("New Desc", task.getDescription()),
                () -> assertEquals(Status.DONE, task.getStatus()),
                () -> assertEquals(newDuration, task.getDuration()),
                () -> assertEquals(newTime, task.getStartTime())
        );
    }

    @Test
    void subtaskEqualsByIdOnly() {
        Subtask subtask1 = new Subtask(1, "Sub 1", "Desc 1", Status.NEW, 10, testDuration, testTime);
        Subtask subtask2 = new Subtask(1, "Sub 2", "Desc 2", Status.DONE, 20, Duration.ofHours(1), testTime.plusDays(1));
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны, независимо от других полей");
    }

    @Test
    void epicEqualsByIdOnly() {
        Epic epic1 = new Epic(1, "Epic 1", "Desc 1");
        Epic epic2 = new Epic(1, "Epic 2", "Desc 2");
        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны, независимо от других полей");
    }

    @Test
    void epicTimeCalculation() {
        Epic epic = new Epic(1, "Epic", "Desc");
        assertNull(epic.getStartTime(), "У нового эпика startTime должен быть null");
        assertNull(epic.getEndTime(), "У нового эпика endTime должен быть null");
        assertEquals(Duration.ZERO, epic.getDuration(), "У нового эпика duration должен быть 0");
    }
}