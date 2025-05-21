package tasktracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.entity.Epic;
import tasktracker.entity.Status;
import tasktracker.entity.Subtask;
import tasktracker.entity.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldAddAndFindTaskById() {
        Task task = manager.createTask("Test", "Desc", Status.NEW);
        Task found = manager.getTask(task.getId());
        assertEquals(task, found, "Задача не найдена по id");
    }

    @Test
    void shouldAddAndFindAllTaskTypes() {
        Task task = manager.createTask("Task", "Desc", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Desc");
        Subtask subtask = manager.createSubtask("Sub", "Desc", Status.NEW, epic.getId());

        assertAll(
                () -> assertEquals(task, manager.getTask(task.getId())),
                () -> assertEquals(epic, manager.getEpic(epic.getId())),
                () -> assertEquals(subtask, manager.getSubtask(subtask.getId()))
        );
    }
}