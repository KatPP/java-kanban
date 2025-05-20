package tasktracker.service;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Возвращаем менеджер с хранением в памяти
    }
}