package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
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
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
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
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                exchange.close();
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
} 