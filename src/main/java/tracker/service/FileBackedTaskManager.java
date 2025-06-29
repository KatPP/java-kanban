package tracker.service;

import tracker.entity.*;
import tracker.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Менеджер задач с автосохранением состояния в файл (CSV-формат).
 * Наследует функциональность {@link InMemoryTaskManager} и добавляет логику работы с файлом.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    /**
     * Создает менеджер с привязкой к файлу для автосохранения.
     * @param file файл для хранения данных (если не существует, будет создан).
     */
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл.
     * Вызывается автоматически после каждой модифицирующей операции.
     *
     * @throws ManagerSaveException если произошла ошибка записи (например, нет прав доступа к файлу).
     */
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // Сериализация задачи в CSV
    private String toString(Task task) {
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(String.valueOf(task.getId()))
                .add(getTaskType(task).name())
                .add(task.getName())
                .add(task.getStatus().name())
                .add(task.getDescription());

        if (task instanceof Subtask) {
            joiner.add(String.valueOf(((Subtask) task).getEpicId()));
        } else {
            joiner.add("");
        }
        return joiner.toString();
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Epic) return TaskType.EPIC;
        if (task instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }

    // Десериализация задачи из CSV
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    /**
     * Загружает данные менеджера из файла.
     *
     * @param file файл с сохраненными данными.
     * @return восстановленный {@link FileBackedTaskManager}.
     * @throws ManagerSaveException если файл не существует или содержит некорректные данные.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) return manager;

            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    // Переопределённые методы TaskManager с автосохранением
    @Override
    public Task createTask(String name, String description, Status status) {
        Task task = super.createTask(name, description, status);
        save();
        return task;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Subtask createSubtask(String name, String description, Status status, int epicId) {
        Subtask subtask = super.createSubtask(name, description, status, epicId);
        save();
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Epic createEpic(String name, String description) {
        Epic epic = super.createEpic(name, description);
        save();
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }
}
