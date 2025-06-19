package tracker.service;

import org.junit.jupiter.api.Test;
import tracker.entity.Status;
import tracker.entity.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyPreservesTaskState() {
        HistoryManager history = Managers.getDefaultHistory();
        Task original = new Task(1, "Original", "Desc", Status.NEW);

        history.add(original);
        Task saved = history.getHistory().get(0);

        assertAll(
                () -> assertEquals(original.getId(), saved.getId()),
                () -> assertEquals(original.getName(), saved.getName()),
                () -> assertEquals(original.getDescription(), saved.getDescription()),
                () -> assertEquals(original.getStatus(), saved.getStatus())
        );
    }

    @Test
    void shouldNotAllowDuplicates() {
        HistoryManager history = Managers.getDefaultHistory();
        Task task = new Task(1, "Task", "Desc", Status.NEW);
        history.add(task);
        history.add(task); // Дублирующий вызов
        assertEquals(1, history.getHistory().size(), "История содержит дубликаты");
    }
}