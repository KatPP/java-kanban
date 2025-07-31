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
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            String[] pathParts = requestPath.split("/");

            switch (requestMethod) {
                case "GET":
                    if (pathParts.length == 2) {
                        // GET /subtasks
                        sendJsonResponse(exchange, manager.getAllSubtasks());
                    } else if (pathParts.length == 3) {
                        // GET /subtasks/{id}
                        try {
                            int subtaskId = Integer.parseInt(pathParts[2]);
                            Optional<Subtask> subtask = Optional.ofNullable(manager.getSubtask(subtaskId));
                            if (subtask.isPresent()) {
                                sendJsonResponse(exchange, subtask.get());
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
                    // POST /subtasks
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);

                    int subtaskId = 0;
                    if (jsonObject.has("id") && !jsonObject.get("id").isJsonNull()) {
                        subtaskId = jsonObject.get("id").getAsInt();
                    }

                    String subtaskName = jsonObject.get("name").getAsString();
                    String subtaskDescription = jsonObject.get("description").getAsString();
                    // Преобразуем строковый статус в enum Status
                    String statusString = jsonObject.get("status").getAsString();
                    Status subtaskStatus;
                    try {
                        subtaskStatus = Status.valueOf(statusString.toUpperCase()); // Преобразуем в верхний регистр для надежности
                    } catch (IllegalArgumentException illegalArgumentException) {
                        String errorMessage = String.format("Неверное значение статуса: %s", statusString);
                        sendText(exchange, errorMessage, 400);
                        return; // Прерываем обработку
                    }
                    int epicId = jsonObject.get("epicId").getAsInt();

                    Duration subtaskDuration = null;
                    if (jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
                        subtaskDuration = Duration.ofMillis(jsonObject.get("duration").getAsLong());
                    }

                    LocalDateTime subtaskStartTime = null;
                    if (jsonObject.has("startTime") && !jsonObject.get("startTime").isJsonNull()) {
                        // Предполагается, что время в формате ISO, как в LocalDateTimeTypeAdapter
                        subtaskStartTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
                    }

                    if (subtaskId == 0) {
                        // Создание новой подзадачи
                        try {
                            Subtask createdSubtask = manager.createSubtask(subtaskName, subtaskDescription, subtaskStatus, epicId, subtaskDuration, subtaskStartTime);
                            // Возвращаем созданную подзадачу в теле ответа
                            sendCreated(exchange, createdSubtask);
                        } catch (ManagerSaveException saveException) { // Обрабатываем ManagerSaveException от менеджера
                            sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                        } catch (Exception generalException) { // Другие исключения
                            handleException(exchange, generalException);
                        }
                    } else {
                        // Обновление существующей подзадачи
                        Subtask existingSubtask = manager.getSubtask(subtaskId);
                        if (existingSubtask != null) {
                            // Проверяем, изменился ли epicId
                            if (existingSubtask.getEpicId() != epicId) {
                                sendText(exchange, "Нельзя изменить epicId подзадачи. Создайте новую подзадачу.", 400);
                                return;
                            }

                            // Создаем обновленную подзадачу с тем же ID
                            // EpicId берется из существующей подзадачи, так как менять его нельзя
                            Subtask updatedSubtask = new Subtask(
                                    subtaskId,
                                    subtaskName,
                                    subtaskDescription,
                                    subtaskStatus,
                                    existingSubtask.getEpicId(), // Не меняем epicId
                                    subtaskDuration,
                                    subtaskStartTime
                            );

                            try {
                                manager.updateSubtask(updatedSubtask);
                                // Возвращаем обновленную подзадачу в теле ответа
                                sendCreated(exchange, updatedSubtask);
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
                        // DELETE /subtasks/{id}
                        try {
                            int idToDelete = Integer.parseInt(pathParts[2]);
                            manager.deleteSubtask(idToDelete);
                            sendSuccess(exchange, "Подзадача удалена");
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