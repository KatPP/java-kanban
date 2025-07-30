package tracker.http.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Status;
import tracker.entity.Task;
import tracker.service.TaskManager;
import tracker.exceptions.ManagerSaveException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Обработчик HTTP-запросов для задач (эндпоинты /tasks).
 */
public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            switch (method) {
                case "GET":
                    if (parts.length == 2) {
                        // GET /tasks
                        sendJsonResponse(exchange, manager.getAllTasks());
                    } else if (parts.length == 3) {
                        // GET /tasks/{id}
                        try {
                            int id = Integer.parseInt(parts[2]);
                            Optional<Task> task = Optional.ofNullable(manager.getTask(id));
                            if (task.isPresent()) {
                                sendJsonResponse(exchange, task.get());
                            } else {
                                sendNotFound(exchange);
                            }
                        } catch (NumberFormatException e) {
                            sendText(exchange, "Неверный формат ID", 400);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    // POST /tasks
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

                    int taskId = 0;
                    if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                        taskId = jsonObject.get("id").getAsInt();
                    }

                    String name = jsonObject.get("name").getAsString();
                    String description = jsonObject.get("description").getAsString();
                    // Преобразуем строковый статус в enum Status
                    String statusStr = jsonObject.get("status").getAsString();
                    Status status;
                    try {
                        status = Status.valueOf(statusStr.toUpperCase()); // Преобразуем в верхний регистр для надежности
                    } catch (IllegalArgumentException e) {
                        sendText(exchange, "Неверное значение статуса: " + statusStr, 400);
                        return; // Прерываем обработку
                    }

                    Duration duration = null;
                    if (jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
                        duration = Duration.ofMillis(jsonObject.get("duration").getAsLong());
                    }

                    LocalDateTime startTime = null;
                    if (jsonObject.has("startTime") && !jsonObject.get("startTime").isJsonNull()) {
                        startTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
                    }

                    if (taskId == 0) {
                        // Создание новой задачи
                        try {
                            Task createdTask = manager.createTask(name, description, status, duration, startTime);
                            // Возвращаем созданную задачу в теле ответа
                            sendCreated(exchange, createdTask);
                        } catch (ManagerSaveException e) { // Обрабатываем ManagerSaveException от менеджера
                            sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                        } catch (Exception e) { // Другие исключения
                            handleException(exchange, e);
                        }
                    } else {
                        // Обновление существующей задачи
                        Task existingTask = manager.getTask(taskId);
                        if (existingTask != null) {
                            // Создаем обновленную задачу с тем же ID
                            Task updatedTask = new Task(
                                    taskId,
                                    name,
                                    description,
                                    status,
                                    duration,
                                    startTime
                            );
                            try {
                                manager.updateTask(updatedTask);
                                // Возвращаем обновленную задачу в теле ответа
                                sendCreated(exchange, updatedTask);
                            } catch (ManagerSaveException e) { // Обрабатываем ManagerSaveException от менеджера
                                sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                            } catch (Exception e) { // Другие исключения
                                handleException(exchange, e);
                            }
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        // DELETE /tasks/{id}
                        try {
                            int taskIdToDelete = Integer.parseInt(parts[2]);
                            manager.deleteTask(taskIdToDelete);
                            sendSuccess(exchange, "Задача удалена");
                        } catch (NumberFormatException e) {
                            sendText(exchange, "Неверный формат ID", 400);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendText(exchange, "Неверный формат JSON", 400);
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400);
        } catch (Exception e) {
            e.printStackTrace(); // Для отладки
            sendText(exchange, "Ошибка сервера: " + e.getMessage(), 500);
        }
    }
}