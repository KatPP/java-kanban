package tracker.http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Task;
import tracker.service.TaskManager;

import java.io.IOException;
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
                        int id = Integer.parseInt(parts[2]);
                        Optional<Task> task = Optional.ofNullable(manager.getTask(id));
                        if (task.isPresent()) {
                            sendJsonResponse(exchange, task.get());
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
                case "POST":
                    // POST /tasks
                    Task newTask = gson.fromJson(
                            new String(exchange.getRequestBody().readAllBytes()),
                            Task.class
                    );
                    if (newTask.getId() == 0) {
                        manager.createTask(
                                newTask.getName(),
                                newTask.getDescription(),
                                newTask.getStatus(),
                                newTask.getDuration(),
                                newTask.getStartTime()
                        );
                    } else {
                        manager.updateTask(newTask);
                    }
                    sendCreated(exchange);
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        // DELETE /tasks/{id}
                        int id = Integer.parseInt(parts[2]);
                        manager.deleteTask(id);
                        sendSuccess(exchange, "Задача удалена");
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