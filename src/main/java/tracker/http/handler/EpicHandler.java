package tracker.http.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.entity.Epic;
import tracker.exceptions.ManagerSaveException;
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
            String[] pathParts = path.split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        // GET /epics
                        System.out.println("EpicHandler: Обработка GET /epics");
                        sendJsonResponse(exchange, manager.getAllEpics());
                    } else if (pathParts.length == 3) {
                        // GET /epics/{id}
                        System.out.println("EpicHandler: Обработка GET /epics/{id}");
                        try {
                            int epicId = Integer.parseInt(pathParts[2]);
                            Optional<Epic> epic = Optional.ofNullable(manager.getEpic(epicId));
                            if (epic.isPresent()) {
                                sendJsonResponse(exchange, epic.get());
                            } else {
                                sendNotFound(exchange);
                            }
                        } catch (NumberFormatException parseException) {
                            sendText(exchange, "Неверный формат ID", 400);
                        }
                    } else if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                        // GET /epics/{id}/subtasks
                        System.out.println("EpicHandler: Обработка GET /epics/{id}/subtasks");
                        try {
                            int epicId = Integer.parseInt(pathParts[2]);
                            // Проверяем существование эпика
                            if (manager.getEpic(epicId) == null) {
                                System.out.println(String.format("EpicHandler: Эпик с ID %d не найден", epicId));
                                sendNotFound(exchange);
                                return;
                            }
                            sendJsonResponse(exchange, manager.getEpicSubtasks(epicId));
                        } catch (NumberFormatException parseException) {
                            sendText(exchange, "Неверный формат ID эпика", 400);
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                    break;
                case "POST":
                    System.out.println("EpicHandler: Начало обработки POST запроса");
                    System.out.println(String.format("EpicHandler: Путь: %s", path));
                    System.out.println(String.format("EpicHandler: Длина pathParts: %d", pathParts.length));

                    if (pathParts.length == 2) {
                        // POST /epics
                        try {
                            System.out.println("EpicHandler: Обработка POST /epics");
                            // Читаем тело запроса как строку
                            byte[] requestBodyBytes = exchange.getRequestBody().readAllBytes();
                            String requestBody = new String(requestBodyBytes, StandardCharsets.UTF_8);
                            System.out.println(String.format("EpicHandler: Тело запроса: %s", requestBody));

                            // Проверка на пустое тело
                            if (requestBody == null || requestBody.trim().isEmpty()) {
                                System.out.println("EpicHandler: Ошибка - пустое тело запроса");
                                sendText(exchange, "Тело запроса не может быть пустым", 400);
                                return;
                            }

                            // Парсим JSON в JsonObject для извлечения полей
                            System.out.println("EpicHandler: Начинаем парсинг JSON...");
                            JsonObject jsonObject = gson.fromJson(requestBody, JsonObject.class);
                            System.out.println(String.format("EpicHandler: JSON успешно распарсен: %s", jsonObject));

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

                            String epicName = jsonObject.get("name").getAsString();
                            String epicDescription = jsonObject.get("description").getAsString();
                            System.out.println(String.format("EpicHandler: Извлеченные данные: name=%s, description=%s", epicName, epicDescription));

                            System.out.println("EpicHandler: Вызов manager.createEpic...");
                            Epic createdEpic = manager.createEpic(epicName, epicDescription);
                            System.out.println(String.format("EpicHandler: Эпик успешно создан: %s", createdEpic));

                            sendCreated(exchange, createdEpic);
                        } catch (JsonSyntaxException jsonException) {
                            System.err.println(String.format("EpicHandler: Ошибка синтаксиса JSON: %s", jsonException.getMessage()));
                            jsonException.printStackTrace();
                            sendText(exchange, "Неверный формат JSON: " + jsonException.getMessage(), 400);
                        } catch (ManagerSaveException saveException) {
                            // Обрабатываем ManagerSaveException от менеджера
                            sendHasOverlaps(exchange); // Отправляем 406 Not Acceptable
                        } catch (Exception generalException) {
                            // Другие исключения
                            System.err.println("EpicHandler: Неожиданная ошибка при создании эпика:");
                            generalException.printStackTrace();
                            sendText(exchange, "Внутренняя ошибка сервера при создании эпика: " + generalException.getMessage(), 500);
                        }
                    } else {
                        System.out.println(String.format("EpicHandler: Неподдерживаемый путь для POST: %s", path));
                        sendNotFound(exchange);
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        // DELETE /epics/{id}
                        System.out.println("EpicHandler: Обработка DELETE /epics/{id}");
                        try {
                            int epicId = Integer.parseInt(pathParts[2]);
                            manager.deleteEpic(epicId);
                            sendSuccess(exchange, "Эпик удален");
                        } catch (NumberFormatException parseException) {
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
            System.err.println(String.format("EpicHandler: Ошибка синтаксиса JSON на верхнем уровне: %s", jsonException.getMessage()));
            sendText(exchange, "Неверный формат JSON", 400);
        } catch (NumberFormatException parseException) {
            System.err.println(String.format("EpicHandler: Ошибка формата числа: %s", parseException.getMessage()));
            sendText(exchange, "Неверный формат ID", 400);
        } catch (Exception generalException) {
            System.err.println("EpicHandler: Неожиданная ошибка на верхнем уровне:");
            generalException.printStackTrace();
            sendText(exchange, "Ошибка сервера: " + generalException.getMessage(), 500);
        } finally {
            System.out.println("EpicHandler: Завершение обработки запроса");
        }
    }
}