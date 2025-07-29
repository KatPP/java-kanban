package tracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.exceptions.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Базовый обработчик HTTP-запросов.
 * Предоставляет общие методы для отправки ответов и обработки ошибок.
 */
public abstract class BaseHttpHandler {
    protected final Gson gson = new Gson();

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String response) throws IOException {
        sendText(exchange, response, 200);
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, -1);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Ресурс не найден", 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange) throws IOException {
        sendText(exchange, "Задача пересекается по времени", 406);
    }

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof NotFoundException) {
            sendNotFound(exchange);
        } else {
            sendText(exchange, "Ошибка сервера: " + e.getMessage(), 500);
        }
    }
}