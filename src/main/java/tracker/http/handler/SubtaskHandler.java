package tracker.http.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Status;
import tracker.entity.Subtask;
import tracker.service.TaskManager;
import tracker.exceptions.ManagerSaveException; // Добавлен импорт исключения

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Обработчик HTTP-запросов для подзадач (эндпоинты /subtasks).
 */
public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager) {
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
                        // GET /subtasks
                        sendJsonResponse(exchange, manager.getAllSubtasks());
                    } else if (parts.length == 3) {
                        // GET /subtasks/{id}
                        try {
                            int id = Integer.parseInt(parts[2]);
                            Optional<Subtask> subtask = Optional.ofNullable(manager.getSubtask(id));
                            if (subtask.isPresent()) {
                                sendJsonResponse(exchange, subtask.get());
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
                    // POST /subtasks
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

                    int id = 0;
                    if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                        id = jsonObject.get("id").getAsInt();
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
                    int epicId = jsonObject.get("epicId").getAsInt();

                    Duration duration = null;
                    if (jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
                        duration = Duration.ofMillis(jsonObject.get("duration").getAsLong());
                    }

                    LocalDateTime startTime = null;
                    if (jsonObject.has("startTime") && !jsonObject.get("startTime").isJsonNull()) {
                        // Предполагается, что время в формате ISO, как в LocalDateTimeTypeAdapter
                        startTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
                    }

                    if (id == 0) {
                        // Создание новой подзадачи
                        try {
                            Subtask createdSubtask = manager.createSubtask(name, description, status, epicId, duration, startTime);
                            // Возвращаем созданную подзадачу в теле ответа
                            sendCreated(exchange, createdSubtask);
                        } catch (ManagerSaveException e) { // Обрабатываем ManagerSaveException от менеджера
                            sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                        } catch (Exception e) { // Другие исключения
                            handleException(exchange, e);
                        }
                    } else {
                        // Обновление существующей подзадачи
                        Subtask existingSubtask = manager.getSubtask(id);
                        if (existingSubtask != null) {
                            // Проверяем, изменился ли epicId
                            if (existingSubtask.getEpicId() != epicId) {
                                sendText(exchange, "Нельзя изменить epicId подзадачи. Создайте новую подзадачу.", 400);
                                return;
                            }

                            // Создаем обновленную подзадачу с тем же ID
                            // EpicId берется из существующей подзадачи, так как менять его нельзя
                            Subtask updatedSubtask = new Subtask(
                                    id,
                                    name,
                                    description,
                                    status,
                                    existingSubtask.getEpicId(), // Не меняем epicId
                                    duration,
                                    startTime
                            );

                            try {
                                manager.updateSubtask(updatedSubtask);
                                // Возвращаем обновленную подзадачу в теле ответа
                                sendCreated(exchange, updatedSubtask);
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
                        // DELETE /subtasks/{id}
                        try {
                            int idToDelete = Integer.parseInt(parts[2]);
                            manager.deleteSubtask(idToDelete);
                            sendSuccess(exchange, "Подзадача удалена");
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