package tracker.service;

import org.junit.jupiter.api.Test;
import tracker.entity.Status;
import tracker.entity.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final LocalDateTime testTime = LocalDateTime.now();
    private final Duration testDuration = Duration.ofMinutes(30);

    @Test
    void historyPreservesTaskState() {
        HistoryManager history = Managers.getDefaultHistory();
        Task original = new Task(1, "Original", "Desc", Status.NEW, testDuration, testTime);

        history.add(original);
        Task saved = history.getHistory().get(0);

        assertAll(
                () -> assertEquals(original.getId(), saved.getId()),
                () -> assertEquals(original.getName(), saved.getName()),
                () -> assertEquals(original.getDescription(), saved.getDescription()),
                () -> assertEquals(original.getStatus(), saved.getStatus()),
                () -> assertEquals(original.getDuration(), saved.getDuration()),
                () -> assertEquals(original.getStartTime(), saved.getStartTime()),
                () -> assertEquals(original.getEndTime(), saved.getEndTime())
        );
    }

    @Test
    void shouldNotAllowDuplicates() {
        HistoryManager history = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Desc", Status.NEW, testDuration, testTime);

        history.add(task);
        history.add(task); // Дублирующий вызов

        assertEquals(1, history.getHistory().size(), "История содержит дубликаты");
    }

    @Test
    void shouldHandleNullTimeFields() {
        HistoryManager history = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Desc", Status.NEW, null, null);

        history.add(task);
        Task saved = history.getHistory().get(0);

        assertAll(
                () -> assertNull(saved.getDuration()),
                () -> assertNull(saved.getStartTime()),
                () -> assertNull(saved.getEndTime())
        );
    }

    @Test
    void shouldMaintainInsertionOrder() {
        HistoryManager history = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task1", "Desc", Status.NEW, testDuration, testTime);
        Task task2 = new Task(2, "Task2", "Desc", Status.IN_PROGRESS,
                Duration.ofHours(1), testTime.plusHours(2));

        history.add(task1);
        history.add(task2);

        List<Task> tasks = history.getHistory();
        assertEquals(2, tasks.size());
        assertEquals(task1.getId(), tasks.get(0).getId());
        assertEquals(task2.getId(), tasks.get(1).getId());
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        HistoryManager history = Managers.getDefaultHistory();
        Task task1 = new Task(1, "Task1", "Desc", Status.NEW, testDuration, testTime);
        Task task2 = new Task(2, "Task2", "Desc", Status.DONE, null, null);

        history.add(task1);
        history.add(task2);
        history.remove(1);

        List<Task> tasks = history.getHistory();
        assertEquals(1, tasks.size());
        assertEquals(task2.getId(), tasks.get(0).getId());
    }
}