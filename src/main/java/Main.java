import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;
import tracker.service.FileBackedTaskManager;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Текущее время для теста
        LocalDateTime now = LocalDateTime.now();

        // Создаём задачи с временными параметрами
        Task task = manager.createTask(
                "Task 1",
                "Description",
                Status.NEW,
                Duration.ofMinutes(30),  // продолжительность 30 минут
                now.plusHours(1)         // начать через 1 час
        );

        Epic epic = manager.createEpic("Epic 1", "Description");

        Subtask subtask = manager.createSubtask(
                "Subtask 1",
                "Description",
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(45),  // продолжительность 45 минут
                now.plusHours(2)         // начать через 2 часа
        );

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем
        System.out.println("Задачи после загрузки:");
        System.out.println("Task: " + loadedManager.getTask(task.getId()));
        System.out.println("Epic: " + loadedManager.getEpic(epic.getId()));
        System.out.println("Subtask: " + loadedManager.getSubtask(subtask.getId()));

        // Проверка временных параметров эпика
        System.out.println("\nВременные параметры эпика:");
        System.out.println("Start time: " + loadedManager.getEpic(epic.getId()).getStartTime());
        System.out.println("Duration: " + loadedManager.getEpic(epic.getId()).getDuration());
        System.out.println("End time: " + loadedManager.getEpic(epic.getId()).getEndTime());

        // Приоритизированный список
        System.out.println("\nПриоритизированные задачи:");
        loadedManager.getPrioritizedTasks().forEach(t ->
                System.out.println(t.getName() + " (start: " + t.getStartTime() + ")")
        );
    }
}