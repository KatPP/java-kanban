import tasktracker.entity.Epic;
import tasktracker.entity.Status;
import tasktracker.entity.Subtask;
import tasktracker.entity.Task;
import tasktracker.service.Managers;
import tasktracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault(); // <- Используем Managers

        // Создаём задачи и эпики
        Task task1 = manager.createTask("Помыть посуду", "Помыть всю посуду вечером", Status.NEW);
        Epic epic1 = manager.createEpic("Организовать праздник", "Подготовка к дню рождения");
        Subtask subtask1 = manager.createSubtask("Купить продукты", "Составить список и купить", Status.NEW, epic1.getId());

        // Просматриваем задачи (добавляем в историю)
        System.out.println(manager.getTask(task1.getId())); // Добавится в историю
        System.out.println(manager.getEpic(epic1.getId())); // Добавится в историю
        System.out.println(manager.getSubtask(subtask1.getId())); // Добавится в историю

        // Выводим историю
        System.out.println("История просмотров:");
        manager.getHistory().forEach(System.out::println);
    }
}