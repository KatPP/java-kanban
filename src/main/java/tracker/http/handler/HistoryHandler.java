package tracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import tracker.service.TaskManager;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для истории просмотров (эндпоинт /history).
 */
public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                // GET /history
                String response = gson.toJson(manager.getHistory());
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}