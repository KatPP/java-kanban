package tracker.service;

import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {
    List<Task> getHistory();

    // Методы для Task
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    Task createTask(String name, String description, Status status);

    Task createTask(String name, String description, Status status,
                    Duration duration, LocalDateTime startTime);

    void updateTask(Task task);

    void deleteTask(int id);

    // Методы для Subtask
    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtask(int id);

    Subtask createSubtask(String name, String description, Status status, int epicId);

    Subtask createSubtask(String name, String description, Status status,
                          int epicId, Duration duration, LocalDateTime startTime);

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

    /**
     * Возвращает список задач, отсортированных по приоритету (времени начала).
     * Задачи без времени начала не включаются в список.
     *
     * @return отсортированный список задач
     */
    List<Task> getPrioritizedTasks();

    /**
     * Проверяет, есть ли пересечение по времени между новой задачей и существующими.
     *
     * @param newTask проверяемая задача
     * @return true если есть пересечение, false если нет
     */
    boolean hasTimeConflict(Task newTask);
}