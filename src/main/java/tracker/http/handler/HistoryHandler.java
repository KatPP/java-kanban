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
            String requestMethod = exchange.getRequestMethod();

            if ("GET".equals(requestMethod)) {
                // GET /history
                String historyResponse = gson.toJson(manager.getHistory());
                sendSuccess(exchange, historyResponse);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception generalException) {
            handleException(exchange, generalException);
        }
    }
}