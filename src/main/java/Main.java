import tracker.entity.Epic;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.entity.Task;
import tracker.service.Managers;
import tracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = manager.createTask("Task 1", "Desc", Status.NEW);
        Task task2 = manager.createTask("Task 2", "Desc", Status.NEW);
        Epic epic1 = manager.createEpic("Epic 1", "Desc");
        Subtask subtask1 = manager.createSubtask("Sub 1", "Desc", Status.NEW, epic1.getId());
        Subtask subtask2 = manager.createSubtask("Sub 2", "Desc", Status.NEW, epic1.getId());
        Epic epic2 = manager.createEpic("Epic 2", "Desc"); // Эпик без подзадач

        // Запрашиваем задачи в разном порядке (с дублированием)
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task1.getId());  // Повторный запрос
        manager.getSubtask(subtask2.getId());

        // Печатаем историю (без дубликатов)
        System.out.println("История:");
        manager.getHistory().forEach(System.out::println);

        // Удаляем задачу и проверяем историю
        manager.deleteTask(task1.getId());
        System.out.println("\nИстория после удаления Task 1:");
        manager.getHistory().forEach(System.out::println);

        // Удаляем эпик с подзадачами
        manager.deleteEpic(epic1.getId());
        System.out.println("\nИстория после удаления Epic 1:");
        manager.getHistory().forEach(System.out::println);
    }
}