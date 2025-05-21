import tasktracker.entity.Epic;
import tasktracker.entity.Status;
import tasktracker.entity.Subtask;
import tasktracker.entity.Task;
import tasktracker.service.Managers;
import tasktracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = manager.createTask("Task 1", "Description", Status.NEW);
        Epic epic1 = manager.createEpic("Epic 1", "Epic desc");
        Subtask subtask1 = manager.createSubtask("Subtask 1", "Sub desc", Status.NEW, epic1.getId());

        // Просматриваем задачи
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());

        // Печатаем историю
        System.out.println("История просмотров:");
        manager.getHistory().forEach(System.out::println);
    }
}