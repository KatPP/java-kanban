import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;
import tracker.service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаём задачи
        Task task = manager.createTask("Task 1", "Description", Status.NEW);
        Epic epic = manager.createEpic("Epic 1", "Description");
        Subtask subtask = manager.createSubtask("Subtask 1", "Description", Status.NEW, epic.getId());

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем
        System.out.println("Задачи после загрузки:");
        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
    }
}