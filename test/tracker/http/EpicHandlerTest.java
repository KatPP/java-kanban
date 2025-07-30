package tracker.http;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest extends HttpTaskServerTestBase {

    @BeforeEach
    void clearManager() {
        // Очищаем менеджер перед каждым тестом
        manager.deleteAllEpics();
    }

    @Test
    @DisplayName("Получение несуществующего эпика возвращает статус 404")
    void shouldReturn404WhenGetNonExistentEpic() throws Exception {
        // Пытаемся получить эпик, который не существует
        var response = sendGet("/epics/999");

        // Проверяем, что получили ошибку 404
        assertEquals(404, response.statusCode(), "Ожидался статус 404 для несуществующего эпика");
    }

    @Test
    @DisplayName("Удаление существующего эпика возвращает статус 200")
    void shouldReturn200WhenDeleteExistingEpic() throws Exception {
        // Сначала создаем эпик
        String epicJson = createEpicJson("Эпик для удаления", "Описание");
        sendPost("/epics", epicJson);

        // Удаляем эпик
        var response = sendDelete("/epics/1");

        // Проверяем успешное удаление
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при удалении эпика");
    }

    @Test
    @DisplayName("Получение всех эпиков возвращает статус 200")
    void shouldReturn200WhenGetAllEpics() throws Exception {
        // Создаем несколько эпиков
        String epic1Json = createEpicJson("Эпик 1", "Описание 1");
        String epic2Json = createEpicJson("Эпик 2", "Описание 2");
        sendPost("/epics", epic1Json);
        sendPost("/epics", epic2Json);

        // Получаем все эпики
        var response = sendGet("/epics");

        // Проверяем успешное получение
        assertEquals(200, response.statusCode(), "Ожидался статус 200 при получении всех эпиков");
        assertNotNull(response.body(), "Тело ответа не должно быть пустым");
    }
}