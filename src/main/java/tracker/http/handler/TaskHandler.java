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
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            String[] pathParts = requestPath.split("/");

            switch (requestMethod) {
                case "GET":
                    if (pathParts.length == 2) {
                        // GET /tasks
                        sendJsonResponse(exchange, manager.getAllTasks());
                    } else if (pathParts.length == 3) {
                        // GET /tasks/{id}
                        try {
                            int taskId = Integer.parseInt(pathParts[2]);
                            Optional<Task> task = Optional.ofNullable(manager.getTask(taskId));
                            if (task.isPresent()) {
                                sendJsonResponse(exchange, task.get());
                            } else {
                                sendNotFound(exchange);
                            }
                        } catch (NumberFormatException numberFormatException) {
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

                    String taskName = jsonObject.get("name").getAsString();
                    String taskDescription = jsonObject.get("description").getAsString();
                    // Преобразуем строковый статус в enum Status
                    String statusString = jsonObject.get("status").getAsString();
                    Status taskStatus;
                    try {
                        taskStatus = Status.valueOf(statusString.toUpperCase()); // Преобразуем в верхний регистр для надежности
                    } catch (IllegalArgumentException illegalArgumentException) {
                        String errorMessage = String.format("Неверное значение статуса: %s", statusString);
                        sendText(exchange, errorMessage, 400);
                        return; // Прерываем обработку
                    }

                    Duration taskDuration = null;
                    if (jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
                        taskDuration = Duration.ofMillis(jsonObject.get("duration").getAsLong());
                    }

                    LocalDateTime taskStartTime = null;
                    if (jsonObject.has("startTime") && !jsonObject.get("startTime").isJsonNull()) {
                        taskStartTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
                    }

                    if (taskId == 0) {
                        // Создание новой задачи
                        try {
                            Task createdTask = manager.createTask(taskName, taskDescription, taskStatus, taskDuration, taskStartTime);
                            // Возвращаем созданную задачу в теле ответа
                            sendCreated(exchange, createdTask);
                        } catch (ManagerSaveException saveException) { // Обрабатываем ManagerSaveException от менеджера
                            sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                        } catch (Exception generalException) { // Другие исключения
                            handleException(exchange, generalException);
                        }
                    } else {
                        // Обновление существующей задачи
                        Task existingTask = manager.getTask(taskId);
                        if (existingTask != null) {
                            // Создаем обновленную задачу с тем же ID
                            Task updatedTask = new Task(
                                    taskId,
                                    taskName,
                                    taskDescription,
                                    taskStatus,
                                    taskDuration,
                                    taskStartTime
                            );
                            try {
                                manager.updateTask(updatedTask);
                                // Возвращаем обновленную задачу в теле ответа
                                sendCreated(exchange, updatedTask);
                            } catch (
                                    ManagerSaveException saveException) { // Обрабатываем ManagerSaveException от менеджера
                                sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                            } catch (Exception generalException) { // Другие исключения
                                handleException(exchange, generalException);
                            }
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        // DELETE /tasks/{id}
                        try {
                            int taskIdToDelete = Integer.parseInt(pathParts[2]);
                            manager.deleteTask(taskIdToDelete);
                            sendSuccess(exchange, "Задача удалена");
                        } catch (NumberFormatException numberFormatException) {
                            sendText(exchange, "Неверный формат ID", 400);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (JsonSyntaxException jsonException) {
            sendText(exchange, "Неверный формат JSON", 400);
        } catch (NumberFormatException numberFormatException) {
            sendText(exchange, "Неверный формат ID", 400);
        } catch (Exception generalException) {
            generalException.printStackTrace(); // Для отладки
            String errorMessage = String.format("Ошибка сервера: %s", generalException.getMessage());
            sendText(exchange, errorMessage, 500);
        }
    }
}