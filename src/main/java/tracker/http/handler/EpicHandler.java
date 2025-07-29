package tracker.http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Epic;
import tracker.service.TaskManager;

import java.io.IOException;
import java.util.Optional;

/**
 * Обработчик HTTP-запросов для эпиков (эндпоинты /epics).
 */
public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicHandler(TaskManager manager) {
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
                        // GET /epics
                        sendJsonResponse(exchange, manager.getAllEpics());
                    } else if (parts.length == 3) {
                        // GET /epics/{id}
                        int id = Integer.parseInt(parts[2]);
                        Optional<Epic> epic = Optional.ofNullable(manager.getEpic(id));
                        if (epic.isPresent()) {
                            sendJsonResponse(exchange, epic.get());
                        } else {
                            sendNotFound(exchange);
                        }
                    } else if (parts.length == 4 && "subtasks".equals(parts[3])) {
                        // GET /epics/{id}/subtasks
                        int epicId = Integer.parseInt(parts[2]);
                        sendJsonResponse(exchange, manager.getEpicSubtasks(epicId));
                    }
                    break;
                case "POST":
                    // POST /epics
                    Epic newEpic = gson.fromJson(
                            new String(exchange.getRequestBody().readAllBytes()),
                            Epic.class
                    );
                    manager.createEpic(newEpic.getName(), newEpic.getDescription());
                    sendCreated(exchange);
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        // DELETE /epics/{id}
                        int id = Integer.parseInt(parts[2]);
                        manager.deleteEpic(id);
                        sendSuccess(exchange, "Эпик удален");
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