import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));

        // Конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // Создаем HTTP-запрос
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // Вызываем REST API для создания задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode());

        // Проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddTaskWithTimeIntersection() throws IOException, InterruptedException {
        // Создаем первую задачу
        Task task1 = new Task("Task 1", "First task", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addNewTask(task1);

        // Создаем вторую задачу с пересекающимся временем
        Task task2 = new Task("Task 2", "Second task", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setDuration(Duration.ofMinutes(60));

        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа 406 (пересечение)
        assertEquals(406, response.statusCode());
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        // Добавляем задачу через менеджер
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        manager.addNewTask(task);

        // Создаем HTTP-запрос
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // Вызываем REST API для получения задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        assertNotNull(response.body());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // Добавляем задачу через менеджер
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        manager.addNewTask(task);
        int taskId = task.getId();

        // Создаем HTTP-запрос
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // Вызываем REST API для получения задачи по ID
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(200, response.statusCode());

        // Проверяем содержимое ответа
        assertNotNull(response.body());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void testGetNonExistentTask() throws IOException, InterruptedException {
        // Создаем HTTP-запрос для несуществующей задачи
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        // Вызываем REST API
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа 404
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // Добавляем задачу
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        manager.addNewTask(task);
        int taskId = task.getId();

        // Удаляем задачу через API
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(200, response.statusCode());

        // Проверяем, что задача удалена
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic", Status.NEW);
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getEpics().size());
        assertEquals("Test Epic", manager.getEpics().get(0).getName());
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        // Сначала создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic", Status.NEW);
        manager.addNewEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW, epic.getId());
        subtask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask.setDuration(Duration.ofMinutes(30));

        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getSubtasks().size());
        assertEquals("Test Subtask", manager.getSubtasks().get(0).getName());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Test Epic", "Testing epic", Status.NEW);
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW, epic.getId());
        manager.addNewSubtask(subtask);

        // Получаем подзадачи эпика
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // Добавляем задачу и получаем её (чтобы добавить в историю)
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        manager.addNewTask(task);
        manager.getTaskById(task.getId());

        // Получаем историю
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Добавляем задачу с временем
        Task task = new Task("Test Task", "Testing task", Status.NEW);
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(task);

        // Получаем приоритетные задачи
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }
} 