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
            if ("GET".equals(exchange.getRequestMethod())) {
                // GET /prioritized
                String response = gson.toJson(manager.getPrioritizedTasks());
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}
