package tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void findTaskById() {
        Task task = manager.createTask("Test", "Desc", Status.NEW);
        Task found = manager.getTask(task.getId());
        assertEquals(task, found, "Задача не найдена по id");
    }

    @Test
    void manageAllTaskTypes() {
        Task task = manager.createTask("Task", "Desc", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Desc");
        Subtask subtask = manager.createSubtask("Sub", "Desc", Status.NEW, epic.getId());

        assertAll(
                () -> assertEquals(task, manager.getTask(task.getId())),
                () -> assertEquals(epic, manager.getEpic(epic.getId())),
                () -> assertEquals(subtask, manager.getSubtask(subtask.getId()))
        );
    }

    @Test
    void removedTaskShouldNotAppearInHistory() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask("Task", "Desc", Status.NEW);
        manager.getTask(task.getId()); // Добавляем в историю
        manager.deleteTask(task.getId());
        assertFalse(manager.getHistory().contains(task), "Задача осталась в истории после удаления");
    }

    @Test
    void epicDeletionRemovesSubtasksFromHistory() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic("Epic", "Desc");
        Subtask subtask = manager.createSubtask("Sub", "Desc", Status.NEW, epic.getId());
        manager.getEpic(epic.getId());      // Добавляем эпик в историю
        manager.getSubtask(subtask.getId()); // Добавляем подзадачу
        manager.deleteEpic(epic.getId());   // Удаляем эпик
        assertAll(
                () -> assertFalse(manager.getHistory().contains(epic)),
                () -> assertFalse(manager.getHistory().contains(subtask))
        );
    }
}