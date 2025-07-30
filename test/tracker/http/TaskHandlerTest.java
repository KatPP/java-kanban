package tracker.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest extends HttpTaskServerTestBase {

    @BeforeEach
    void clearManager() {
        // Очищаем менеджер перед каждым тестом
        manager.deleteAllTasks();
    }

    // --- Позитивные (Positive) тесты ---

    @Test
    @DisplayName("Создание задачи возвращает статус 201 и созданную задачу")
    void shouldReturn201AndCreatedTaskWhenCreateTask() throws Exception {
        // Подготавливаем данные для создания задачи
        String taskJson = createTaskJson("Новая задача", "Описание новой задачи", "NEW");

        // Отправляем запрос на создание задачи
        var response = sendPost("/tasks", taskJson);

        // Проверяем успешное создание
        assertEquals(201, response.statusCode(), "Ожидался статус 201 при создании задачи. Ответ: " + response.body());

        // Проверяем, что тело ответа не пустое и содержит данные задачи
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Новая задача"), "Ответ должен содержать имя созданной задачи");
        assertTrue(response.body().contains("\"id\":")); // Проверяем наличие ID
    }

    @Test
    @DisplayName("Получение существующей задачи по ID возвращает статус 200 и данные задачи")
    void shouldReturn200AndTaskDataWhenGetExistingTask() throws Exception {
        // Сначала создаем задачу
        String taskJson = createTaskJson("Задача для получения", "Описание задачи", "IN_PROGRESS");
        var createResponse = sendPost("/tasks", taskJson);
        assertEquals(201, createResponse.statusCode(), "Задача должна быть успешно создана");

        // Извлекаем ID созданной задачи
        JsonObject createdTaskJson = JsonParser.parseString(createResponse.body()).getAsJsonObject();
        int taskId = createdTaskJson.get("id").getAsInt();

        // Запрашиваем задачу по ID
        var response = sendGet("/tasks/" + taskId);

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении задачи. Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Задача для получения"), "Ответ должен содержать данные запрошенной задачи");
        assertTrue(response.body().contains("\"id\":" + taskId), "Ответ должен содержать правильный ID задачи");
    }

    @Test
    @DisplayName("Обновление существующей задачи возвращает статус 201 и обновленные данные")
    void shouldReturn201AndUpdatedDataWhenUpdateExistingTask() throws Exception {
        // 1. Создаем задачу
        String initialTaskJson = createTaskJson("Исходная задача", "Исходное описание", "NEW");
        var createResponse = sendPost("/tasks", initialTaskJson);
        assertEquals(201, createResponse.statusCode(), "Задача должна быть успешно создана");

        // Извлекаем ID созданной задачи
        JsonObject createdTaskJson = JsonParser.parseString(createResponse.body()).getAsJsonObject();
        int taskId = createdTaskJson.get("id").getAsInt();

        // 2. Подготавливаем данные для обновления
        String updatedTaskJson = """
                {
                    "id": %d,
                    "name": "Обновленная задача",
                    "description": "Новое описание задачи",
                    "status": "DONE"
                }""".formatted(taskId);

        // 3. Отправляем запрос на обновление
        var updateResponse = sendPost("/tasks", updatedTaskJson);

        // 4. Проверяем успешное обновление
        assertEquals(201, updateResponse.statusCode(), "Ожидался статус 201 при обновлении задачи. Ответ: " + updateResponse.body());
        assertNotNull(updateResponse.body(), "Тело ответа не должно быть пустым");
        assertTrue(updateResponse.body().contains("Обновленная задача"), "Ответ должен содержать обновленное имя задачи");
        assertTrue(updateResponse.body().contains("\"status\":\"DONE\""), "Ответ должен содержать обновленный статус");
    }

    @Test
    @DisplayName("Удаление существующей задачи по ID возвращает статус 200")
    void shouldReturn200WhenDeleteExistingTask() throws Exception {
        // Сначала создаем задачу
        String taskJson = createTaskJson("Задача для удаления", "Описание", "NEW");
        var createResponse = sendPost("/tasks", taskJson);
        assertEquals(201, createResponse.statusCode(), "Задача должна быть успешно создана");

        // Извлекаем ID созданной задачи
        JsonObject createdTaskJson = JsonParser.parseString(createResponse.body()).getAsJsonObject();
        int taskId = createdTaskJson.get("id").getAsInt();

        // Удаляем задачу
        var response = sendDelete("/tasks/" + taskId);

        // Проверяем успешное удаление
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при удалении задачи. Ответ: " + response.body());
        assertTrue(response.body().contains("Задача удалена"), "Ответ должен подтверждать удаление");
    }

    @Test
    @DisplayName("Получение всех задач возвращает статус 200 и список задач")
    void shouldReturn200AndTaskListWhenGetAllTasks() throws Exception {
        // Создаем несколько задач
        String task1Json = createTaskJson("Задача 1", "Описание 1", "NEW");
        String task2Json = createTaskJson("Задача 2", "Описание 2", "IN_PROGRESS");
        sendPost("/tasks", task1Json);
        sendPost("/tasks", task2Json);

        // Получаем все задачи
        var response = sendGet("/tasks");

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении всех задач. Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Задача 1"), "Ответ должен содержать 'Задача 1'");
        assertTrue(response.body().contains("Задача 2"), "Ответ должен содержать 'Задача 2'");
    }

    @Test
    @DisplayName("Получение всех задач когда их нет возвращает статус 200 и пустой список")
    void shouldReturn200AndEmptyListWhenGetAllTasksAndNoneExist() throws Exception {
        // Убеждаемся, что задач нет (очистка выполнена в @BeforeEach)

        // Получаем все задачи
        var response = sendGet("/tasks");

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении пустого списка задач. Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        // Ожидаем пустой список в формате JSON
        assertEquals("[]", response.body().trim(), "Ответ должен быть пустым списком JSON");
    }

    // --- Негативные (Negative) тесты ---

    @Test
    @DisplayName("Получение задачи с некорректным ID (не числом) возвращает статус 400")
    void shouldReturn400WhenGetTaskWithInvalidId() throws Exception {
        // Пытаемся получить задачу с некорректным ID
        var response = sendGet("/tasks/abc");

        // Проверяем, что получили ошибку 400
        assertEquals(400, response.statusCode(), "Ожидался статус 400 для некорректного ID. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Получение несуществующей задачи возвращает статус 404")
    void shouldReturn404WhenGetNonExistentTask() throws Exception {
        // Пытаемся получить задачу, которая не существует
        var response = sendGet("/tasks/999999"); // Используем большой ID, маловероятно, что он существует

        // Проверяем, что получили ошибку 404
        assertEquals(404, response.statusCode(), "Ожидался статус 404 для несуществующей задачи. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Создание задачи с некорректным JSON возвращает статус 400")
    void shouldReturn400WhenCreateTaskWithInvalidJson() throws Exception {
        // Подготавливаем некорректный JSON
        String invalidTaskJson = "{ invalid json }";

        // Отправляем запрос на создание задачи
        var response = sendPost("/tasks", invalidTaskJson);

        // Проверяем, что получили ошибку 400
        assertEquals(400, response.statusCode(), "Ожидался статус 400 для некорректного JSON. Ответ: " + response.body());
        assertTrue(response.body().contains("Неверный формат JSON") || response.body().contains("invalid") || response.body().contains("JSON"), "Ответ должен упоминать ошибку JSON");
    }


    @Test
    @DisplayName("Обновление задачи с несуществующим ID возвращает статус 404")
    void shouldReturn404WhenUpdateNonExistentTask() throws Exception {
        // Подготавливаем данные для обновления несуществующей задачи
        String updateTaskJson = """
                {
                    "id": 999999,
                    "name": "Попытка обновления",
                    "description": "Описание",
                    "status": "DONE"
                }""";

        // Отправляем запрос на обновление
        var response = sendPost("/tasks", updateTaskJson);

        // Проверяем, что получили ошибку 404
        assertEquals(404, response.statusCode(), "Ожидался статус 404 при попытке обновления несуществующей задачи. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Удаление задачи с некорректным ID (не числом) возвращает статус 400")
    void shouldReturn400WhenDeleteTaskWithInvalidId() throws Exception {
        // Пытаемся удалить задачу с некорректным ID
        var response = sendDelete("/tasks/xyz");

        // Проверяем, что получили ошибку 400
        assertEquals(400, response.statusCode(), "Ожидался статус 400 для некорректного ID при удалении. Ответ: " + response.body());
    }
}