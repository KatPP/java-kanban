package tracker.service;

import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getHistory(); // <- Новый метод
    // Методы для Task
    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task createTask(String name, String description, Status status);
    void updateTask(Task task);
    void deleteTask(int id);

    // Методы для Subtask
    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtask(int id);
    Subtask createSubtask(String name, String description, Status status, int epicId);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);

    // Методы для Epic
    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic createEpic(String name, String description);
    void updateEpic(Epic epic);
    void deleteEpic(int id);
    List<Subtask> getEpicSubtasks(int epicId);
}