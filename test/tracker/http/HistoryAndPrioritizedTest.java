package tracker.http;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class HistoryAndPrioritizedTest extends HttpTaskServerTestBase {

    @BeforeEach
    void clearManager() {
        // Очищаем все данные перед каждым тестом
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    @Test
    @DisplayName("Получение истории просмотров возвращает статус 200")
    void shouldReturn200WhenGetHistory() throws Exception {
        // Создаем задачу
        String taskJson = createTaskJson("Задача для истории", "Описание задачи", "NEW");
        sendPost("/tasks", taskJson);

        // Просматриваем задачу (добавляем в историю)
        sendGet("/tasks/1");

        // Получаем историю просмотров
        var response = sendGet("/history");

        // Проверяем успешное получение истории
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении истории");
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }

    @Test
    @DisplayName("Получение истории при отсутствии просмотров возвращает статус 200")
    void shouldReturn200ForEmptyHistory() throws Exception {
        // Получаем историю без предварительных просмотров
        var response = sendGet("/history");

        // Проверяем, что получили пустую историю (не ошибку)
        assertEquals(200, response.statusCode(), "Ожидался статус 200 для пустой истории");
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }

    @Test
    @DisplayName("Получение списка приоритетных задач возвращает статус 200")
    void shouldReturn200WhenGetPrioritized() throws Exception {
        // Создаем задачу с временными параметрами для приоритезации
        String taskJson = """
                {
                    "name": "Приоритетная задача",
                    "description": "Описание задачи",
                    "status": "NEW",
                    "duration": 3600000,
                    "startTime": "2023-01-01T12:00:00"
                }""";

        sendPost("/tasks", taskJson);

        // Получаем список приоритетных задач
        var response = sendGet("/prioritized");

        // Проверяем успешное получение списка
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении приоритетных задач");
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }

    @Test
    @DisplayName("Получение пустого списка приоритетных задач возвращает статус 200")
    void shouldReturn200ForEmptyPrioritized() throws Exception {
        // Получаем список приоритетных задач без предварительно созданных задач
        var response = sendGet("/prioritized");

        // Проверяем, что получили пустой список (не ошибку)
        assertEquals(200, response.statusCode(), "Ожидался статус 200 для пустого списка приоритетов");
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }
}