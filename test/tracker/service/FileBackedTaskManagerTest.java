package tracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.entity.Status;
import tracker.entity.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @DisplayName("Проверяем, что задачи корректно сохраняются в файл и восстанавливаются из него")
    @Test
    void shouldSaveAndLoadTasksFromFile() throws IOException {
        // Создаем временный файл
        File file = File.createTempFile("tasks", ".csv");

        // Создаем менеджер и добавляем задачи с временными параметрами
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task1 = manager.createTask(
                "Task 1",
                "Description 1",
                Status.NEW,
                Duration.ofMinutes(30),  // продолжительность 30 минут
                LocalDateTime.now()      // время начала сейчас
        );
        Task task2 = manager.createTask(
                "Task 2",
                "Description 2",
                Status.IN_PROGRESS,
                Duration.ofHours(1),     // продолжительность 1 час
                LocalDateTime.now().plusHours(1)  // начать через 1 час
        );

        // Загружаем данные из файла в новый менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что задачи загрузились корректно
        assertEquals(2, loadedManager.getAllTasks().size(), "Количество задач не совпадает");
        assertEquals(task1.getName(), loadedManager.getTask(task1.getId()).getName(), "Название задачи 1 не совпадает");
        assertEquals(task1.getDuration(), loadedManager.getTask(task1.getId()).getDuration(), "Продолжительность задачи 1 не совпадает");
        assertEquals(task1.getStartTime(), loadedManager.getTask(task1.getId()).getStartTime(), "Время начала задачи 1 не совпадает");

        assertEquals(task2.getStatus(), loadedManager.getTask(task2.getId()).getStatus(), "Статус задачи 2 не совпадает");
        assertEquals(task2.getDuration(), loadedManager.getTask(task2.getId()).getDuration(), "Продолжительность задачи 2 не совпадает");
        assertEquals(task2.getStartTime(), loadedManager.getTask(task2.getId()).getStartTime(), "Время начала задачи 2 не совпадает");
    }

    @DisplayName("Проверка, на то что менеджер корректно работает с пустым файлом.")
    @Test
    void shouldHandleEmptyFile() throws IOException {
        // Создаем пустой временный файл
        File file = File.createTempFile("empty", ".csv");

        // Загружаем менеджер из пустого файла
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что менеджер пустой
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(manager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(manager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }
}