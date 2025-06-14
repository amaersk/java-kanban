import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final Gson gson;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = createGson();
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public static Gson getGson() {
        return createGson();
    }

    private static Gson createGson() {
        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).registerTypeAdapter(Duration.class, new DurationAdapter()).create();
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        // Регистрируем обработчики для каждого пути
        httpServer.createContext("/tasks", new TaskHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));

        httpServer.start();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    // Базовый обработчик HTTP запросов
    abstract static class BaseHttpHandler {
        protected void sendText(HttpExchange h, String text) throws IOException {
            byte[] resp = text.getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(200, resp.length);
            h.getResponseBody().write(resp);
            h.close();
        }

        protected void sendCreated(HttpExchange h, String text) throws IOException {
            byte[] resp = text.getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(201, resp.length);
            h.getResponseBody().write(resp);
            h.close();
        }

        protected void sendNotFound(HttpExchange h) throws IOException {
            h.sendResponseHeaders(404, 0);
            h.close();
        }

        protected void sendHasInteractions(HttpExchange h) throws IOException {
            h.sendResponseHeaders(406, 0);
            h.close();
        }

        protected void sendInternalServerError(HttpExchange h) throws IOException {
            h.sendResponseHeaders(500, 0);
            h.close();
        }
    }

    // Исключение для случаев, когда ресурс не найден
    static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    // Обработчик задач
    static class TaskHandler extends BaseHttpHandler implements HttpHandler {
        private final TaskManager taskManager;
        private final Gson gson;

        public TaskHandler(TaskManager taskManager, Gson gson) {
            this.taskManager = taskManager;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                switch (method) {
                    case "GET":
                        handleGet(exchange, path);
                        break;
                    case "POST":
                        handlePost(exchange);
                        break;
                    case "DELETE":
                        handleDelete(exchange, path);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0);
                        exchange.close();
                }
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange);
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }

        private void handleGet(HttpExchange exchange, String path) throws IOException {
            if (path.equals("/tasks")) {
                // GET /tasks - получить все задачи
                List<Task> tasks = taskManager.getTasks();
                String response = gson.toJson(tasks);
                sendText(exchange, response);
            } else if (path.startsWith("/tasks/")) {
                // GET /tasks/{id} - получить задачу по ID
                String idStr = path.substring("/tasks/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Task task = taskManager.getTaskById(id);
                    if (task == null) {
                        throw new NotFoundException("Задача не найдена");
                    }
                    String response = gson.toJson(task);
                    sendText(exchange, response);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                // Создание новой задачи
                taskManager.addNewTask(task);
                String response = gson.toJson(task);
                sendCreated(exchange, response);
            } else {
                // Обновление существующей задачи
                taskManager.updateTask(task);
                String response = gson.toJson(task);
                sendCreated(exchange, response);
            }
        }

        private void handleDelete(HttpExchange exchange, String path) throws IOException {
            if (path.startsWith("/tasks/")) {
                String idStr = path.substring("/tasks/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    taskManager.deleteTask(id);
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }
    }

    // Обработчик подзадач
    static class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
        private final TaskManager taskManager;
        private final Gson gson;

        public SubtaskHandler(TaskManager taskManager, Gson gson) {
            this.taskManager = taskManager;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                switch (method) {
                    case "GET":
                        handleGet(exchange, path);
                        break;
                    case "POST":
                        handlePost(exchange);
                        break;
                    case "DELETE":
                        handleDelete(exchange, path);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0);
                        exchange.close();
                }
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange);
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }

        private void handleGet(HttpExchange exchange, String path) throws IOException {
            if (path.equals("/subtasks")) {
                // GET /subtasks - получить все подзадачи
                List<Subtask> subtasks = taskManager.getSubtasks();
                String response = gson.toJson(subtasks);
                sendText(exchange, response);
            } else if (path.startsWith("/subtasks/")) {
                // GET /subtasks/{id} - получить подзадачу по ID
                String idStr = path.substring("/subtasks/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Subtask subtask = taskManager.getSubtaskById(id);
                    if (subtask == null) {
                        throw new NotFoundException("Подзадача не найдена");
                    }
                    String response = gson.toJson(subtask);
                    sendText(exchange, response);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                // Создание новой подзадачи
                taskManager.addNewSubtask(subtask);
                String response = gson.toJson(subtask);
                sendCreated(exchange, response);
            } else {
                // Обновление существующей подзадачи
                taskManager.updateSubtask(subtask);
                String response = gson.toJson(subtask);
                sendCreated(exchange, response);
            }
        }

        private void handleDelete(HttpExchange exchange, String path) throws IOException {
            if (path.startsWith("/subtasks/")) {
                String idStr = path.substring("/subtasks/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    taskManager.deleteSubtask(id);
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }
    }

    // Обработчик эпиков
    static class EpicHandler extends BaseHttpHandler implements HttpHandler {
        private final TaskManager taskManager;
        private final Gson gson;

        public EpicHandler(TaskManager taskManager, Gson gson) {
            this.taskManager = taskManager;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                switch (method) {
                    case "GET":
                        handleGet(exchange, path);
                        break;
                    case "POST":
                        handlePost(exchange);
                        break;
                    case "DELETE":
                        handleDelete(exchange, path);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, 0);
                        exchange.close();
                }
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            } catch (IllegalArgumentException e) {
                sendHasInteractions(exchange);
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }

        private void handleGet(HttpExchange exchange, String path) throws IOException {
            if (path.equals("/epics")) {
                // GET /epics - получить все эпики
                List<Epic> epics = taskManager.getEpics();
                String response = gson.toJson(epics);
                sendText(exchange, response);
            } else if (path.startsWith("/epics/") && path.endsWith("/subtasks")) {
                // GET /epics/{id}/subtasks - получить подзадачи эпика
                String pathWithoutSubtasks = path.substring(0, path.length() - "/subtasks".length());
                String idStr = pathWithoutSubtasks.substring("/epics/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Epic epic = taskManager.getEpicById(id);
                    if (epic == null) {
                        throw new NotFoundException("Эпик не найден");
                    }
                    List<Subtask> subtasks = taskManager.printArrayIdSubtask(id);
                    String response = gson.toJson(subtasks);
                    sendText(exchange, response);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else if (path.startsWith("/epics/")) {
                // GET /epics/{id} - получить эпик по ID
                String idStr = path.substring("/epics/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Epic epic = taskManager.getEpicById(id);
                    if (epic == null) {
                        throw new NotFoundException("Эпик не найден");
                    }
                    String response = gson.toJson(epic);
                    sendText(exchange, response);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            Epic epic = gson.fromJson(body, Epic.class);

            // Создание нового эпика (эпики не обновляются через POST)
            taskManager.addNewEpic(epic);
            String response = gson.toJson(epic);
            sendCreated(exchange, response);
        }

        private void handleDelete(HttpExchange exchange, String path) throws IOException {
            if (path.startsWith("/epics/")) {
                String idStr = path.substring("/epics/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    taskManager.deleteEpic(id);
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        }
    }

    // Обработчик истории
    static class HistoryHandler extends BaseHttpHandler implements HttpHandler {
        private final TaskManager taskManager;
        private final Gson gson;

        public HistoryHandler(TaskManager taskManager, Gson gson) {
            this.taskManager = taskManager;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equals(method) && "/history".equals(path)) {
                    // GET /history - получить историю просмотров
                    List<Task> history = taskManager.getHistory();
                    String response = gson.toJson(history);
                    sendText(exchange, response);
                } else {
                    exchange.sendResponseHeaders(405, 0);
                    exchange.close();
                }
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }
    }

    // Обработчик приоритетных задач
    static class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
        private final TaskManager taskManager;
        private final Gson gson;

        public PrioritizedHandler(TaskManager taskManager, Gson gson) {
            this.taskManager = taskManager;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                if ("GET".equals(method) && "/prioritized".equals(path)) {
                    // GET /prioritized - получить задачи в порядке приоритета
                    Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                    String response = gson.toJson(prioritizedTasks);
                    sendText(exchange, response);
                } else {
                    exchange.sendResponseHeaders(405, 0);
                    exchange.close();
                }
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }
    }

    // Адаптер для LocalDateTime
    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(localDateTime.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            String dateTime = jsonReader.nextString();
            return LocalDateTime.parse(dateTime, formatter);
        }
    }

    // Адаптер для Duration
    static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            if (duration == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(duration.toMinutes());
            }
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            long minutes = jsonReader.nextLong();
            return Duration.ofMinutes(minutes);
        }
    }
} 