package tracker.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import tracker.service.InMemoryTaskManager;
import tracker.service.TaskManager;
import tracker.util.DurationTypeAdapter;
import tracker.util.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServerTestBase {
    protected static HttpTaskServer server;
    protected static TaskManager manager;
    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    protected static final HttpClient client = HttpClient.newHttpClient();
    protected static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void startServer() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    protected HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        URI url = URI.create(BASE_URL + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendPost(String path, String jsonBody) throws IOException, InterruptedException {
        URI url = URI.create(BASE_URL + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        URI url = URI.create(BASE_URL + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Вспомогательный метод для создания JSON задачи
    protected String createTaskJson(String name, String description, String status) {
        JsonObject task = new JsonObject();
        task.addProperty("name", name);
        task.addProperty("description", description);
        task.addProperty("status", status);
        return gson.toJson(task);
    }

    // Вспомогательный метод для создания JSON эпика
    protected String createEpicJson(String name, String description) {
        JsonObject epic = new JsonObject();
        epic.addProperty("name", name);
        epic.addProperty("description", description);
        return gson.toJson(epic);
    }

    // Вспомогательный метод для создания JSON подзадачи
    protected String createSubtaskJson(String name, String description, String status, int epicId) {
        JsonObject subtask = new JsonObject();
        subtask.addProperty("name", name);
        subtask.addProperty("description", description);
        subtask.addProperty("status", status);
        subtask.addProperty("epicId", epicId);
        subtask.add("duration", null);
        subtask.add("startTime", null);
        return gson.toJson(subtask);
    }
}