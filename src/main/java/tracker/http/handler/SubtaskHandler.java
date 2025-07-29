package tracker.http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Subtask;
import tracker.service.TaskManager;

import java.io.IOException;
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
                        int id = Integer.parseInt(parts[2]);
                        Optional<Subtask> subtask = Optional.ofNullable(manager.getSubtask(id));
                        if (subtask.isPresent()) {
                            sendJsonResponse(exchange, subtask.get());
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "POST":
                    // POST /subtasks
                    Subtask newSubtask = gson.fromJson(
                            new String(exchange.getRequestBody().readAllBytes()),
                            Subtask.class
                    );
                    if (newSubtask.getId() == 0) {
                        manager.createSubtask(
                                newSubtask.getName(),
                                newSubtask.getDescription(),
                                newSubtask.getStatus(),
                                newSubtask.getEpicId(),
                                newSubtask.getDuration(),
                                newSubtask.getStartTime()
                        );
                    } else {
                        manager.updateSubtask(newSubtask);
                    }
                    sendCreated(exchange);
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        // DELETE /subtasks/{id}
                        int id = Integer.parseInt(parts[2]);
                        manager.deleteSubtask(id);
                        sendSuccess(exchange, "Подзадача удалена");
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendText(exchange, "Неверный формат JSON", 400);
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}