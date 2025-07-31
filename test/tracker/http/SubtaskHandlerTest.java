package tracker.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHandlerTest extends HttpTaskServerTestBase {

    private int testEpicId;

    @BeforeEach
    void setUp() throws Exception {
        // Очищаем менеджер перед каждым тестом
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        // manager.deleteAllTasks(); // Если нужно очищать и задачи

        // Создаем тестовый эпик для подзадач
        String epicJson = createEpicJson("Тестовый эпик", "Эпик для подзадач");
        var response = sendPost("/epics", epicJson);

        // Проверяем успешное создание эпика
        assertEquals(201, response.statusCode(), "Эпик должен быть успешно создан. Ответ: " + response.body());

        // Извлекаем реальный ID созданного эпика из тела ответа
        String responseBody = response.body();
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonElement idElement = jsonObject.get("id");
                if (idElement != null && !idElement.isJsonNull()) {
                    testEpicId = idElement.getAsInt();
                } else {
                    fail("Ответ на создание эпика не содержит ID. Тело ответа: " + responseBody);
                }
            } catch (Exception e) {
                // Если тело ответа не JSON объект, попробуем получить ID из менеджера
                // Это запасной вариант, лучше, чтобы сервер возвращал ID
                // Для тестов предположим, что ID будет следующим после очистки
                testEpicId = 1;
                System.err.println("Не удалось распарсить ID из ответа. Используем ID по умолчанию: " + testEpicId + ". Ошибка: " + e.getMessage());
            }
        } else {
            // Если сервер не возвращает тело при 201, предполагаем ID
            testEpicId = 1;
            System.err.println("Пустое тело ответа при создании эпика. Используем ID по умолчанию: " + testEpicId);
        }

        System.out.println("Тестовый эпик создан с ID: " + testEpicId);
    }

    @Test
    @DisplayName("Создание подзадачи возвращает статус 201")
    void shouldReturn201WhenCreateSubtask() throws Exception {
        // Подготавливаем данные для создания подзадачи
        String subtaskJson = """
                {
                    "name": "Тестовая подзадача",
                    "description": "Описание подзадачи",
                    "status": "NEW",
                    "epicId": %d
                }""".formatted(testEpicId);

        // Отправляем запрос на создание подзадачи
        var response = sendPost("/subtasks", subtaskJson);

        // Проверяем успешное создание
        assertEquals(201, response.statusCode(), "Ожидался статус 201 при создании подзадачи. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Получение существующей подзадачи возвращает статус 200")
    void shouldReturn200WhenGetExistingSubtask() throws Exception {
        // Сначала создаем подзадачу
        String subtaskJson = """
        {
            "name": "Тестовая подзадача",
            "description": "Описание",
            "status": "NEW",
            "epicId": %d
        }""".formatted(testEpicId);

        var createResponse = sendPost("/subtasks", subtaskJson);
        assertEquals(201, createResponse.statusCode(), "Подзадача должна быть успешно создана. Ответ: " + createResponse.body());

        // Извлекаем реальный ID созданной подзадачи из тела ответа
        int createdSubtaskId = -1; // Инициализируем недопустимым значением
        String createResponseBody = createResponse.body();
        if (createResponseBody != null && !createResponseBody.isEmpty()) {
            try {
                JsonObject createdSubtaskJson = JsonParser.parseString(createResponseBody).getAsJsonObject();
                JsonElement idElement = createdSubtaskJson.get("id");
                if (idElement != null && !idElement.isJsonNull()) {
                    createdSubtaskId = idElement.getAsInt();
                } else {
                    fail("Ответ на создание подзадачи не содержит ID. Тело ответа: " + createResponseBody);
                }
            } catch (Exception e) {
                fail("Не удалось извлечь ID подзадачи из ответа. Тело ответа: " + createResponseBody + ". Ошибка: " + e.getMessage());
            }
        } else {
            fail("Пустое тело ответа при создании подзадачи. Ожидался JSON с данными созданной подзадачи.");
        }

        // Затем запрашиваем её по реальному ID
        var response = sendGet("/subtasks/" + createdSubtaskId);

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении подзадачи с ID " + createdSubtaskId + ". Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }

    @Test
    @DisplayName("Получение несуществующей подзадачи возвращает статус 404")
    void shouldReturn404WhenGetNonExistentSubtask() throws Exception {
        // Пытаемся получить подзадачу, которая не существует
        var response = sendGet("/subtasks/999");

        // Проверяем, что получили ошибку 404
        assertEquals(404, response.statusCode(), "Ожидался статус 404 для несуществующей подзадачи. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Получение подзадач эпика возвращает статус 200")
    void shouldReturn200WhenGetEpicSubtasks() throws Exception {
        // Сначала создаем подзадачу
        String subtaskJson = """
                {
                    "name": "Подзадача эпика",
                    "description": "Описание",
                    "status": "NEW",
                    "epicId": %d
                }""".formatted(testEpicId);
        var createResponse = sendPost("/subtasks", subtaskJson);
        assertEquals(201, createResponse.statusCode(), "Подзадача должна быть успешно создана");

        // Получаем все подзадачи эпика
        var response = sendGet("/epics/" + testEpicId + "/subtasks");

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении подзадач эпика. Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
        // Можно также проверить, что в ответе есть созданная подзадача
        assertTrue(response.body().contains("Подзадача эпика"), "Ответ должен содержать созданную подзадачу");
    }

    @Test
    @DisplayName("Получение подзадач несуществующего эпика возвращает статус 404")
    void shouldReturn404WhenGetSubtasksOfNonExistentEpic() throws Exception {
        // Пытаемся получить подзадачи несуществующего эпика
        var response = sendGet("/epics/999/subtasks");

        // Проверяем, что получили ошибку 404
        assertEquals(404, response.statusCode(), "Ожидался статус 404 для подзадач несуществующего эпика. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Удаление существующей подзадачи возвращает статус 200")
    void shouldReturn200WhenDeleteExistingSubtask() throws Exception {
        // Сначала создаем подзадачу
        String subtaskJson = """
                {
                    "name": "Подзадача для удаления",
                    "description": "Описание",
                    "status": "NEW",
                    "epicId": %d
                }""".formatted(testEpicId);
        var createResponse = sendPost("/subtasks", subtaskJson);
        assertEquals(201, createResponse.statusCode(), "Подзадача должна быть успешно создана");

        // Удаляем подзадачу (предполагаем ID = 1)
        var response = sendDelete("/subtasks/1");

        // Проверяем успешное удаление
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при удалении подзадачи. Ответ: " + response.body());
    }

    @Test
    @DisplayName("Получение всех подзадач возвращает статус 200")
    void shouldReturn200WhenGetAllSubtasks() throws Exception {
        // Создаем несколько подзадач
        String subtask1Json = """
                {
                    "name": "Подзадача 1",
                    "description": "Описание 1",
                    "status": "NEW",
                    "epicId": %d
                }""".formatted(testEpicId);
        String subtask2Json = """
                {
                    "name": "Подзадача 2",
                    "description": "Описание 2",
                    "status": "IN_PROGRESS",
                    "epicId": %d
                }""".formatted(testEpicId);
        sendPost("/subtasks", subtask1Json);
        sendPost("/subtasks", subtask2Json);

        // Получаем все подзадачи
        var response = sendGet("/subtasks");

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении всех подзадач. Ответ: " + response.body());
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Подзадача 1"), "Ответ должен содержать 'Подзадача 1'");
        assertTrue(response.body().contains("Подзадача 2"), "Ответ должен содержать 'Подзадача 2'");
    }
}