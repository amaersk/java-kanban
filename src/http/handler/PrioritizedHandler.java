package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Set;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
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
                // GET /prioritized - получить приоритетные задачи
                Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                String response = gson.toJson(prioritizedTasks);
                sendText(exchange, response);
            } else {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
                exchange.close();
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}