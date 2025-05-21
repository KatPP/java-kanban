package tasktracker.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");
        assertTrue(manager.getAllTasks().isEmpty(), "Новый менеджер должен быть пустым");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history, "Менеджер истории не должен быть null");
        assertTrue(history.getHistory().isEmpty(), "Новая история должна быть пустой");
    }

}