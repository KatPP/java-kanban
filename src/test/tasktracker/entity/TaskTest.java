package tasktracker.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Another desc", Status.DONE);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }
    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(1, "Sub 1", "Desc 1", Status.NEW, 10);
        Subtask subtask2 = new Subtask(1, "Sub 2", "Desc 2", Status.DONE, 20);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic(1, "Epic 1", "Desc 1");
        Epic epic2 = new Epic(1, "Epic 2", "Desc 2");
        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }
}
