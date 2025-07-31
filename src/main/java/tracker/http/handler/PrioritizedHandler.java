package tracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import tracker.service.TaskManager;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для приоритетных задач (эндпоинт /prioritized).
 */
public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String requestMethod = exchange.getRequestMethod();

            if ("GET".equals(requestMethod)) {
                // GET /prioritized
                String prioritizedTasksResponse = gson.toJson(manager.getPrioritizedTasks());
                sendSuccess(exchange, prioritizedTasksResponse);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception generalException) {
            handleException(exchange, generalException);
        }
    }
}