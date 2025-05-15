import tasktracker.entity.Epic;
import tasktracker.entity.Status;
import tasktracker.entity.Subtask;
import tasktracker.entity.Task;
import tasktracker.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("Создаем задачи и эпики");

        // Создаем две задачи
        Task task1 = manager.createTask("Помыть посуду", "Помыть всю посуду вечером", Status.NEW);
        Task task2 = manager.createTask("Позвонить другу", "Обсудить планы на выходные", Status.NEW);

        // Создаем первый эпик с двумя подзадачами
        Epic epic1 = manager.createEpic("Организовать праздник", "Подготовка к дню рождения");
        Subtask subtask1 = manager.createSubtask("Купить продукты", "Составить список и купить", Status.NEW, epic1.getId());
        Subtask subtask2 = manager.createSubtask("Приготовить ужин", "Пожарить мясо", Status.NEW, epic1.getId());

        // Создаем второй эпик с одной подзадачей
        Epic epic2 = manager.createEpic("Переезд", "Подготовка к переезду в новую квартиру");
        Subtask subtask3 = manager.createSubtask("Упаковать книги", "Упаковать все книги в коробки", Status.NEW, epic2.getId());

        printAllTasks(manager);

        System.out.println("Изменяем статусы.");

        // Меняем статусы
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);
        task2.setStatus(Status.IN_PROGRESS);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask3);


        printAllTasks(manager);

        System.out.println("Удаляем одну задачу и один эпик.");

        // Удаляем задачу и эпик
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic2.getId());


        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Все задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Все эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Все подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        // Печатаем подзадачи для каждого эпика
        System.out.println("Подзадачи для эпиков:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("Эпик [" + epic.getName() + "]:");
            manager.getEpicSubtasks(epic.getId()).forEach(System.out::println);
        }
    }
}