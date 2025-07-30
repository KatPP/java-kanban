package tracker.http.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Epic;
import tracker.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                        System.out.println("EpicHandler: Обработка GET /epics");
                        sendJsonResponse(exchange, manager.getAllEpics());
                    } else if (parts.length == 3) {
                        // GET /epics/{id}
                        System.out.println("EpicHandler: Обработка GET /epics/{id}");
                        try {
                            int id = Integer.parseInt(parts[2]);
                            Optional<Epic> epic = Optional.ofNullable(manager.getEpic(id));
                            if (epic.isPresent()) {
                                sendJsonResponse(exchange, epic.get());
                            } else {
                                sendNotFound(exchange);
                            }
                        } catch (NumberFormatException e) {
                            sendText(exchange, "Неверный формат ID", 400);
                        }
                    } else if (parts.length == 4 && "subtasks".equals(parts[3])) {
                        // GET /epics/{id}/subtasks
                        System.out.println("EpicHandler: Обработка GET /epics/{id}/subtasks");
                        try {
                            int epicId = Integer.parseInt(parts[2]);
                            // Проверяем существование эпика
                            if (manager.getEpic(epicId) == null) {
                                System.out.println("EpicHandler: Эпик с ID " + epicId + " не найден");
                                sendNotFound(exchange);
                                return;
                            }
                            sendJsonResponse(exchange, manager.getEpicSubtasks(epicId));
                        } catch (NumberFormatException e) {
                            sendText(exchange, "Неверный формат ID эпика", 400);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    System.out.println("EpicHandler: Начало обработки POST запроса");
                    System.out.println("EpicHandler: Путь: " + path);
                    System.out.println("EpicHandler: Длина parts: " + parts.length);

                    if (parts.length == 2) {
                        // POST /epics
                        try {
                            System.out.println("EpicHandler: Обработка POST /epics");
                            // Читаем тело запроса как строку
                            byte[] requestBodyBytes = exchange.getRequestBody().readAllBytes();
                            String requestBody = new String(requestBodyBytes, StandardCharsets.UTF_8);
                            System.out.println("EpicHandler: Тело запроса: " + requestBody);

                            // Проверка на пустое тело
                            if (requestBody == null || requestBody.trim().isEmpty()) {
                                System.out.println("EpicHandler: Ошибка - пустое тело запроса");
                                sendText(exchange, "Тело запроса не может быть пустым", 400);
                                return;
                            }

                            // Парсим JSON в JsonObject для извлечения полей
                            System.out.println("EpicHandler: Начинаем парсинг JSON...");
                            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
                            System.out.println("EpicHandler: JSON успешно распарсен: " + jsonObject);

                            // Проверяем наличие обязательных полей
                            if (jsonObject == null) {
                                System.out.println("EpicHandler: Ошибка - не удалось распарсить JSON в объект");
                                sendText(exchange, "Неверный формат JSON", 400);
                                return;
                            }

                            if (!jsonObject.has("name")) {
                                System.out.println("EpicHandler: Ошибка - отсутствует поле 'name'");
                                sendText(exchange, "Отсутствует обязательное поле: name", 400);
                                return;
                            }

                            if (!jsonObject.has("description")) {
                                System.out.println("EpicHandler: Ошибка - отсутствует поле 'description'");
                                sendText(exchange, "Отсутствует обязательное поле: description", 400);
                                return;
                            }

                            String name = jsonObject.get("name").getAsString();
                            String description = jsonObject.get("description").getAsString();
                            System.out.println("EpicHandler: Извлеченные данные: name=" + name + ", description=" + description);

                            System.out.println("EpicHandler: Вызов manager.createEpic...");
                            Epic createdEpic = manager.createEpic(name, description);
                            System.out.println("EpicHandler: Эпик успешно создан: " + createdEpic);

                            sendCreated(exchange, createdEpic);
                        } catch (JsonSyntaxException e) {
                            System.err.println("EpicHandler: Ошибка синтаксиса JSON: " + e.getMessage());
                            e.printStackTrace();
                            sendText(exchange, "Неверный формат JSON: " + e.getMessage(), 400);
                        } catch (Exception e) {
                            System.err.println("EpicHandler: Неожиданная ошибка при создании эпика:");
                            e.printStackTrace(); // Это выведет стек исключения в консоль сервера
                            sendText(exchange, "Внутренняя ошибка сервера при создании эпика: " + e.getMessage(), 500);
                        }
                    } else {
                        System.out.println("EpicHandler: Неподдерживаемый путь для POST: " + path);
                        sendNotFound(exchange);
                    }
                    break;
                case "DELETE":
                    if (parts.length == 3) {
                        // DELETE /epics/{id}
                        System.out.println("EpicHandler: Обработка DELETE /epics/{id}");
                        try {
                            int id = Integer.parseInt(parts[2]);
                            manager.deleteEpic(id);
                            sendSuccess(exchange, "Эпик удален");
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
            System.err.println("EpicHandler: Ошибка синтаксиса JSON на верхнем уровне: " + e.getMessage());
            sendText(exchange, "Неверный формат JSON", 400);
        } catch (NumberFormatException e) {
            System.err.println("EpicHandler: Ошибка формата числа: " + e.getMessage());
            sendText(exchange, "Неверный формат ID", 400);
        } catch (Exception e) {
            System.err.println("EpicHandler: Неожиданная ошибка на верхнем уровне:");
            e.printStackTrace(); // Для отладки
            sendText(exchange, "Ошибка сервера: " + e.getMessage(), 500);
        } finally {
            System.out.println("EpicHandler: Завершение обработки запроса");
        }
    }
}