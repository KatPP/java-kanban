package tracker.service;

import org.junit.jupiter.api.Test;
import tracker.entity.Status;
import tracker.entity.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyLimitedTo10Tasks() {
        HistoryManager history = Managers.getDefaultHistory();
        for (int i = 1; i <= 15; i++) {
            history.add(new Task(i, "Task " + i, "Desc", Status.NEW));
        }
        assertEquals(10, history.getHistory().size(), "История должна хранить только 10 задач");
        assertEquals("Task 6", history.getHistory().get(0).getName(), "Первая задача должна быть Task 6");
    }

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
}